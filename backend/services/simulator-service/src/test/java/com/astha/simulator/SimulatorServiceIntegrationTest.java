package com.astha.simulator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SimulatorServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SIM-1 to SIM-6: Seeded synthetic population determinism and fraud typology injection")
    void shouldGenerateSeededPopulationAndInjectTypologies() throws Exception {
        // 1. Reset with Seed 42
        mockMvc.perform(post("/v1/simulator/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"seed": 42}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(44))) // 30 customers + 8 agents + 6 merchants
                .andExpect(jsonPath("$[0].district").isNotEmpty())
                .andExpect(jsonPath("$[0].msisdn", startsWith("+88017")));

        // 2. Inject Fraud Typology: MULE_CHAIN
        mockMvc.perform(post("/v1/simulator/scenarios/inject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "typology": "MULE_CHAIN",
                  "targetEntityId": "CUST-5"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typology").value("MULE_CHAIN"))
                .andExpect(jsonPath("$.label").value("FRAUD"));

        // 3. Inject Hard Negative Typology: High-volume legitimate merchant
        mockMvc.perform(post("/v1/simulator/scenarios/inject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "typology": "HARD_NEGATIVE",
                  "targetEntityId": "MERCHANT-1"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("HARD_NEGATIVE"));

        // 4. Verify Ground Truth table
        mockMvc.perform(get("/v1/simulator/ground-truth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("EVL-1 to EVL-3 & NFR-15: Deterministic Evaluation across Configurations and 5,000 Dispute Simulation")
    void shouldRunDeterministicEvaluationAcrossConfigurations() throws Exception {
        // Run Evaluation with Config C and seed 42
        mockMvc.perform(post("/v1/simulator/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "config": "CONFIG_C_PLUS_GROUPING",
                  "seed": 42
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuration").value("CONFIG_C_PLUS_GROUPING"))
                .andExpect(jsonPath("$.seed").value(42))
                .andExpect(jsonPath("$.overallPrecision", greaterThan(0.80)))
                .andExpect(jsonPath("$.overallRecall", greaterThan(0.85)))
                .andExpect(jsonPath("$.analystMinutesSaved", greaterThan(1000)))
                .andExpect(jsonPath("$.disputeSimulation.totalDisputesSimulated").value(5000))
                .andExpect(jsonPath("$.disputeSimulation.slaCompliancePercentage").value(96.8))
                .andExpect(jsonPath("$.disputeSimulation.autoRefundSharePercentage").value(42.4))
                .andExpect(jsonPath("$.typologyMetrics", hasSize(11)));
    }
}
