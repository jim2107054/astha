package com.astha.ingest.service;

import com.astha.ingest.validator.TransactionSchemaValidator;
import com.astha.platform.outbox.OutboxEvent;
import com.astha.platform.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private final TransactionSchemaValidator schemaValidator;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${astha.ingest.topic:txn.events.v1}")
    private String ingestTopic;

    public IngestService(TransactionSchemaValidator schemaValidator,
                         OutboxRepository outboxRepository,
                         ObjectMapper objectMapper) {
        this.schemaValidator = schemaValidator;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IngestResult ingestTransaction(String rawPayload, String idempotencyKey) {
        Set<String> validationErrors = schemaValidator.validate(rawPayload);
        if (!validationErrors.isEmpty()) {
            return IngestResult.rejected("Schema validation failed: " + String.join(", ", validationErrors));
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String eventId = root.has("eventId") ? root.get("eventId").asText() : UUID.randomUUID().toString();
            String accountId = root.has("accountId") ? root.get("accountId").asText() : "UNKNOWN";

            // Persist to transactional outbox for at-least-once reliability
            OutboxEvent outboxEvent = new OutboxEvent(
                    "TRANSACTION",
                    accountId, // aggregateId / partition key ensures per-account sequential ordering
                    "TRANSACTION_INGESTED",
                    ingestTopic,
                    rawPayload,
                    UUID.randomUUID().toString()
            );
            outboxRepository.save(outboxEvent);

            // Best-effort immediate dispatch to Kafka if template available
            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send(ingestTopic, accountId, rawPayload);
                } catch (Exception e) {
                    log.warn("Direct Kafka send failed, outbox worker will retry: {}", e.getMessage());
                }
            }

            return IngestResult.accepted(eventId, accountId);
        } catch (Exception e) {
            log.error("Failed to parse and ingest event: {}", e.getMessage(), e);
            return IngestResult.rejected("Malformed transaction event: " + e.getMessage());
        }
    }

    public record IngestResult(boolean accepted, String eventId, String accountId, String errorMessage, Instant timestamp) {
        public static IngestResult accepted(String eventId, String accountId) {
            return new IngestResult(true, eventId, accountId, null, Instant.now());
        }

        public static IngestResult rejected(String errorMessage) {
            return new IngestResult(false, null, null, errorMessage, Instant.now());
        }
    }
}
