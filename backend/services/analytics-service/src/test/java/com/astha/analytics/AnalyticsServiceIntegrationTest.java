package com.astha.analytics;

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
public class AnalyticsServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("EVL-4, EVL-5 & ADM-3: Executive KPIs and Rule Quality Precision Tracking")
    void shouldReturnKpisAndTrackRuleQuality() throws Exception {
        // 1. Check Executive KPIs
        mockMvc.perform(get("/v1/analytics/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLossPreventedPaisa").value(45800000L))
                .andExpect(jsonPath("$.slaComplianceRate").value(96.8))
                .andExpect(jsonPath("$.autoRefundSharePercentage").value(42.0));

        // 2. Check Seeded Rule Quality
        mockMvc.perform(get("/v1/analytics/rules-quality"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[?(@.ruleId == 'R01_PASS_THROUGH_MULE')].precisionRate").value(hasItem(closeTo(0.933, 0.01))));

        // 3. Record Analyst Feedback (True Fraud on R01)
        mockMvc.perform(post("/v1/analytics/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "ruleId": "R01_PASS_THROUGH_MULE",
                  "trueFraud": true
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleId").value("R01_PASS_THROUGH_MULE"))
                .andExpect(jsonPath("$.trueFraudCount").value(113))
                .andExpect(jsonPath("$.totalTriggerCount").value(121));
    }
}
