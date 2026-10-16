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
}
