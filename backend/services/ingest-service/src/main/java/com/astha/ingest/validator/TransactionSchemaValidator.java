package com.astha.ingest.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TransactionSchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonSchema schema;

    @PostConstruct
    public void init() {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            // Search contracts folder locally or in parent
            Path path = Paths.get("contracts/txn.events.v1.json");
            if (!Files.exists(path)) {
                path = Paths.get("../contracts/txn.events.v1.json");
            }
            if (!Files.exists(path)) {
                path = Paths.get("../../contracts/txn.events.v1.json");
            }

            if (Files.exists(path)) {
                try (InputStream is = Files.newInputStream(path)) {
                    this.schema = factory.getSchema(is);
                }
            } else {
                // Inline schema fallback if file not in classpath during tests
                String inline = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "type": "object",
                  "required": ["eventId", "eventType", "timestamp", "accountId", "amountPaisa", "currency", "status"],
                  "properties": {
                    "eventId": { "type": "string" },
                    "eventType": { "type": "string" },
                    "timestamp": { "type": "string" },
                    "accountId": { "type": "string" },
                    "amountPaisa": { "type": "integer", "minimum": 1 },
                    "currency": { "type": "string" },
                    "status": { "type": "string" }
                  }
                }
                """;
                this.schema = factory.getSchema(inline);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load txn.events.v1 schema", e);
        }
    }

    public Set<String> validate(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            Set<ValidationMessage> errors = schema.validate(node);
            return errors.stream().map(ValidationMessage::getMessage).collect(Collectors.toSet());
        } catch (Exception e) {
            return Set.of("Invalid JSON format: " + e.getMessage());
        }
    }
}
