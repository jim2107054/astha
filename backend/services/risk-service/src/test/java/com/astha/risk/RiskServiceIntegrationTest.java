package com.astha.risk;

import com.astha.risk.domain.ScoreResponse;
import com.astha.risk.domain.TransactionScoreRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RiskServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("RSK-1: Synchronous score endpoint returns decision with reason codes and latency")
    void shouldScoreTransactionViaApi() throws Exception {
        TransactionScoreRequest request = new TransactionScoreRequest();
        request.setEventId(UUID.randomUUID().toString());
        request.setAccountId("acc-01712345678");
        request.setAmountPaisa(2500000L);
        request.setEventType("P2P");

        MvcResult result = mockMvc.perform(post("/v1/risk/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "idem-score-100")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").exists())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.reasonCodes").isArray())
                .andReturn();

        ScoreResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ScoreResponse.class);
        assertThat(response.getLatencyMs()).isGreaterThanOrEqualTo(0);

        // Test Idempotency: exact same key and body returns cached response
        mockMvc.perform(post("/v1/risk/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "idem-score-100")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(request.getEventId()));
    }

    @Test
    @DisplayName("RSK-4: Rule listing API returns catalog of all active rules")
    void shouldListRules() throws Exception {
        mockMvc.perform(get("/v1/risk/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @DisplayName("NFR-1: Benchmark decision latency p95 is under 100 ms")
    void shouldMeetP95LatencyBudget() throws Exception {
        List<Long> latencies = new ArrayList<>();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            TransactionScoreRequest req = new TransactionScoreRequest();
            req.setEventId("bench-" + i);
            req.setAccountId("acc-bench-" + (i % 10));
            req.setAmountPaisa(100000L);
            req.setEventType("P2P");

            mockMvc.perform(post("/v1/risk/score")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            latencies.add(elapsedMs);
        }

        Collections.sort(latencies);
        int p95Index = (int) Math.ceil(0.95 * latencies.size()) - 1;
        long p95Latency = latencies.get(p95Index);

        assertThat(p95Latency)
                .as("p95 latency must be strictly under 100 ms as mandated by NFR-1")
                .isLessThan(100);
    }
}
