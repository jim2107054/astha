package com.astha.cases;

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
public class CaseServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    @DisplayName("CAS-1 to CAS-4: Alert grouping, triage queue, four-eyes enforcement, and case disposition")
    void shouldExecuteCompleteCaseManagementLifecycle() throws Exception {
        // 1. Ingest Alert 1 on account acc-mule-99
        mockMvc.perform(post("/v1/cases/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "entityType": "ACCOUNT",
                  "entityId": "acc-mule-99",
                  "ruleId": "R01_RAPID_PASS_THROUGH",
                  "ruleName": "Rapid Pass-through Movement",
                  "riskScore": 85,
                  "amountPaisa": 4500000,
                  "eventId": "evt-01"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.alertCount").value(1));

        // 2. Ingest Alert 2 on same account acc-mule-99 -> Groups into the existing case
        String caseResponse = mockMvc.perform(post("/v1/cases/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "entityType": "ACCOUNT",
                  "entityId": "acc-mule-99",
                  "ruleId": "R04_FAN_IN_FAST_FAN_OUT",
                  "ruleName": "Fan-in Mule Dispersion",
                  "riskScore": 92,
                  "amountPaisa": 3000000,
                  "eventId": "evt-02"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertCount").value(2))
                .andExpect(jsonPath("$.amountAtRiskPaisa").value(7500000))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andReturn().getResponse().getContentAsString();

        // Extract caseId
        String caseId = objectMapper.readTree(caseResponse).get("caseId").asText();

        // 3. Analyst assigns case and adds investigation note
        mockMvc.perform(post("/v1/cases/" + caseId + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-tariq")
                .content("""
                {"assigneeId": "analyst-tariq"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATING"))
                .andExpect(jsonPath("$.assigneeId").value("analyst-tariq"));

        mockMvc.perform(post("/v1/cases/" + caseId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-tariq")
                .content("""
                {"note": "Identified mule pass-through pattern. Requesting account freeze."}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionType").value("COMMENT"));

        // 4. Request Account Freeze Approval
        String approvalResponse = mockMvc.perform(post("/v1/cases/" + caseId + "/approvals")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-tariq")
                .content("""
                {
                  "actionType": "ACCOUNT_FREEZE",
                  "amountPaisa": 7500000,
                  "reason": "Confirmed mule cluster participation"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requesterId").value("analyst-tariq"))
                .andReturn().getResponse().getContentAsString();

        String approvalId = objectMapper.readTree(approvalResponse).get("approvalId").asText();

        // 5. FOUR-EYES TEST: Requester (analyst-tariq) attempts to approve their own request -> MUST BE FORBIDDEN (403)
        mockMvc.perform(post("/v1/cases/approvals/" + approvalId + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-tariq")
                .content("""
                {
                  "approved": true,
                  "decisionNote": "Self approval attempt"
                }
                """))
                .andExpect(status().isForbidden());

        // 6. Independent Approver (manager-hasan) approves request -> SUCCESS
        mockMvc.perform(post("/v1/cases/approvals/" + approvalId + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "manager-hasan")
                .content("""
                {
                  "approved": true,
                  "decisionNote": "Evidence validated against graph. Approved."
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approverId").value("manager-hasan"));

        // 7. Verify Case Report Export
        mockMvc.perform(get("/v1/cases/" + caseId + "/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value(caseId))
                .andExpect(jsonPath("$.alerts", hasSize(2)))
                .andExpect(jsonPath("$.timeline", hasSize(greaterThanOrEqualTo(4))));
    }
}
