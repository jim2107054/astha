package com.astha.disputes;

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
public class DisputeServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    @DisplayName("DSP-1 to DSP-9: File dispute, SLA calculation, state machine transitions, and auto-refund")
    void shouldExecuteCompleteDisputeLifecycle() throws Exception {
        // 1. File Dispute by customer
        String fileResp = mockMvc.perform(post("/v1/disputes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "txnId": "TXN-FAILED-001",
                  "senderAccountId": "acc-customer-01",
                  "counterpartyAccountId": "acc-merchant-01",
                  "disputeType": "FAILED_TXN",
                  "amountPaisa": 150000,
                  "filingSource": "CUSTOMER",
                  "note": "Money debited at ATM but cash did not dispense"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.deadlineUtc").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String disputeId = objectMapper.readTree(fileResp).get("disputeId").asText();

        // 2. Transition from SUBMITTED to UNDER_INVESTIGATION
        mockMvc.perform(post("/v1/disputes/" + disputeId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "targetStatus": "UNDER_INVESTIGATION",
                  "reason": "Officer reviewing ATM telemetry"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_INVESTIGATION"));

        // 3. Resolve Dispute with REFUND_FULL
        mockMvc.perform(post("/v1/disputes/" + disputeId + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "outcome": "REFUND_FULL",
                  "note": "ATM journal confirmed rollback failure. Full refund credited."
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED_REFUNDED"))
                .andExpect(jsonPath("$.resolutionOutcome").value("REFUND_FULL"));

        // 4. State Machine Validation: Attempting illegal transition from terminal state RESOLVED_REFUNDED -> SUBMITTED throws 400
        mockMvc.perform(post("/v1/disputes/" + disputeId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "targetStatus": "SUBMITTED",
                  "reason": "Illegal transition attempt"
                }
                """))
                .andExpect(status().isBadRequest());

        // 5. Append-only Timeline Check
        mockMvc.perform(get("/v1/disputes/" + disputeId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].fromStatus").isEmpty())
                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"));

        // 6. Test DSP-3: Auto-refund engine
        mockMvc.perform(post("/v1/disputes/auto-refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "txnId": "TXN-AUTO-999",
                  "senderAccountId": "acc-user-88",
                  "counterpartyAccountId": "merchant-qr-77",
                  "amountPaisa": 50000
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED_REFUNDED"))
                .andExpect(jsonPath("$.filingSource").value("AUTO_REFUND"))
                .andExpect(jsonPath("$.resolutionOutcome").value("REFUND_FULL"));
    }
}
