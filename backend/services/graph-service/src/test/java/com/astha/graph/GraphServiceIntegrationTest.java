package com.astha.graph;

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
public class GraphServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GRP-1 to GRP-4: Entity graph creation, neighbor traversal, fan-in mule ring detection")
    void shouldCreateGraphAndDetectMuleRing() throws Exception {
        // 1. Create fan-in pattern: 3 victim accounts transfer to mule-account-01
        String[] victims = {"acc-victim-01", "acc-victim-02", "acc-victim-03"};
        for (String victim : victims) {
            mockMvc.perform(post("/v1/graph/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                    {
                      "sourceId": "%s",
                      "targetId": "acc-mule-01",
                      "relationship": "TRANSFERRED_TO",
                      "amountPaisa": 2500000
                    }
                    """, victim)))
                    .andExpect(status().isOk());
        }

        // 2. Fast fan-out: mule-account-01 transfers 7,000,000 paisa out to cashout-agent-dhaka
        mockMvc.perform(post("/v1/graph/edges")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "sourceId": "acc-mule-01",
                  "targetId": "agent-cashout-01",
                  "relationship": "TRANSFERRED_TO",
                  "amountPaisa": 7000000
                }
                """))
                .andExpect(status().isOk());

        // 3. Test GRP-1: Neighbor traversal with depth 2
        mockMvc.perform(get("/v1/graph/entities/acc-mule-01/neighbors?depth=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rootNodeId").value("acc-mule-01"))
                .andExpect(jsonPath("$.nodes", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.edges", hasSize(greaterThanOrEqualTo(4))));

        // 4. Test GRP-2 & GRP-3: Trigger mule ring detection job
        mockMvc.perform(post("/v1/graph/jobs/detect-rings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].typology").value("FAN_IN_FAN_OUT"))
                .andExpect(jsonPath("$[0].riskScore").value(88))
                .andExpect(jsonPath("$[0].evidencePathJson", containsString("acc-mule-01")));

        // 5. Test GRP-4: Query ring candidates
        mockMvc.perform(get("/v1/graph/rings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
