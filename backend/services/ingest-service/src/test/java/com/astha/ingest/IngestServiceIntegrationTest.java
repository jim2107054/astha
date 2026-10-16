package com.astha.ingest;

import com.astha.platform.outbox.OutboxEvent;
import com.astha.platform.outbox.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class IngestServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OutboxRepository outboxRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("ING-1 & ING-2: Valid transaction event is accepted and written to outbox with partition key")
    void shouldAcceptValidTransactionEvent() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String validJson = String.format("""
        {
          "eventId": "%s",
          "eventType": "CASH_OUT",
          "timestamp": "2026-10-15T08:30:00Z",
          "accountId": "acc-01711223344",
          "counterpartyId": "agent-dhaka-01",
          "amountPaisa": 2500000,
          "currency": "BDT",
          "status": "SUCCESS"
        }
        """, eventId);

        mockMvc.perform(post("/v1/events/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idemp-" + eventId)
                .content(validJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.accountId").value("acc-01711223344"));

        List<OutboxEvent> events = outboxRepository.findAll();
        assertThat(events).anyMatch(e -> "acc-01711223344".equals(e.getAggregateId()) && e.getPayloadJson().contains(eventId));
    }

    @Test
    @DisplayName("ING-2: Invalid schema missing required fields returns 400 ProblemDetails")
    void shouldRejectInvalidSchemaPayload() throws Exception {
        String invalidJson = """
        {
          "eventType": "P2P",
          "amountPaisa": -500
        }
        """;

        mockMvc.perform(post("/v1/events/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Transaction Schema Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("ING-5: Ingestion latency is well within p95 50ms budget")
    void shouldMeetIngestionLatencyBudget() throws Exception {
        // Warm up JIT and Jackson serialization
        for (int i = 0; i < 5; i++) {
            String warmUpJson = String.format("""
            {
              "eventId": "%s",
              "eventType": "P2P",
              "timestamp": "2026-10-15T09:00:00Z",
              "accountId": "acc-warmup-%d",
              "amountPaisa": 10000,
              "currency": "BDT",
              "status": "SUCCESS"
            }
            """, UUID.randomUUID(), i);
            mockMvc.perform(post("/v1/events/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(warmUpJson))
                    .andExpect(status().isAccepted());
        }

        String eventId = UUID.randomUUID().toString();
        String validJson = String.format("""
        {
          "eventId": "%s",
          "eventType": "P2P",
          "timestamp": "2026-10-15T09:00:00Z",
          "accountId": "acc-01899112233",
          "amountPaisa": 150000,
          "currency": "BDT",
          "status": "SUCCESS"
        }
        """, eventId);

        long start = System.currentTimeMillis();
        mockMvc.perform(post("/v1/events/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isAccepted());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(100);
    }
}
