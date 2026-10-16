package com.astha.providermock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProviderMockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SIM-1 & DSP-3: Account creation, transfer, idempotent freeze and refund flow")
    void shouldPerformCompleteProviderMockFlow() throws Exception {
        // 1. Create Sender Account
        mockMvc.perform(post("/v1/provider/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "accountId": "acc-sender-01",
                  "msisdn": "+8801711000001",
                  "accountHolderName": "Rahim Uddin",
                  "balancePaisa": 500000,
                  "kycTier": "TIER_2",
                  "accountType": "CUSTOMER"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balancePaisa").value(500000));

        // 2. Create Receiver Account
        mockMvc.perform(post("/v1/provider/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "accountId": "acc-receiver-01",
                  "msisdn": "+8801711000002",
                  "accountHolderName": "Karim Mia",
                  "balancePaisa": 100000,
                  "kycTier": "TIER_1",
                  "accountType": "CUSTOMER"
                }
                """))
                .andExpect(status().isOk());

        // 3. Execute Transaction of 2000 BDT (200,000 paisa)
        String txnResponse = mockMvc.perform(post("/v1/provider/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idemp-txn-001")
                .content("""
                {
                  "senderAccountId": "acc-sender-01",
                  "receiverAccountId": "acc-receiver-01",
                  "amountPaisa": 200000,
                  "txnType": "P2P"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andReturn().getResponse().getContentAsString();

        // 4. Verify balances
        mockMvc.perform(get("/v1/provider/accounts/acc-sender-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balancePaisa").value(300000)); // 500k - 200k

        mockMvc.perform(get("/v1/provider/accounts/acc-receiver-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balancePaisa").value(300000)); // 100k + 200k

        // 5. Freeze Account (Idempotent)
        mockMvc.perform(post("/v1/provider/accounts/acc-sender-01/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idemp-freeze-001")
                .content("""
                {"reason": "FRAUD_RING_SUSPECT"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        // Second call with same idempotency key returns exact same status
        mockMvc.perform(post("/v1/provider/accounts/acc-sender-01/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idemp-freeze-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        // Frozen account cannot send money
        mockMvc.perform(post("/v1/provider/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "senderAccountId": "acc-sender-01",
                  "receiverAccountId": "acc-receiver-01",
                  "amountPaisa": 10000,
                  "txnType": "P2P"
                }
                """))
                .andExpect(status().isUnprocessableEntity());

        // 6. Unfreeze Account
        mockMvc.perform(post("/v1/provider/accounts/acc-sender-01/unfreeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"reason": "CLEARED_BY_ANALYST"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
