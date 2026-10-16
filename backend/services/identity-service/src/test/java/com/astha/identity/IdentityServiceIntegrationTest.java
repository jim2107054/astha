package com.astha.identity;

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
public class IdentityServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("ADM-1 to AUD-2: OTP auth, Staff RBAC, audited PII reveal, and dynamic configs")
    void shouldExecuteIdentityAndAuditLifecycle() throws Exception {
        // 1. Phone OTP Auth flow
        mockMvc.perform(post("/v1/auth/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"phone": "+8801711223344"}
                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "phone": "+8801711223344",
                  "otp": "123456"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // 2. Staff RBAC login
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "email": "farhana@astha.gov.bd",
                  "role": "ANALYST"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ANALYST"));

        // 3. PII reveal without valid reason fails (400)
        mockMvc.perform(post("/v1/audit/reveal-pii")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-farhana")
                .content("""
                {
                  "resourceId": "acc-mule-01",
                  "reason": "no"
                }
                """))
                .andExpect(status().isBadRequest());

        // 4. PII reveal with valid business reason succeeds and returns unmasked value
        mockMvc.perform(post("/v1/audit/reveal-pii")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "analyst-farhana")
                .content("""
                {
                  "resourceId": "acc-mule-01",
                  "reason": "Investigating high velocity cash-out for SAR report",
                  "actualValue": "+8801799887766"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revealedValue").value("+8801799887766"));

        // 5. Verify Audit Log has recorded the PII reveal action
        mockMvc.perform(get("/v1/audit/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].action").value("REVEAL_PII"))
                .andExpect(jsonPath("$[0].actorId").value("analyst-farhana"));

        // 6. Dynamic Admin Config Update
        mockMvc.perform(put("/v1/admin/configs/risk.auto_freeze_threshold")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Actor-Id", "admin-01")
                .content("""
                {"value": "92"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("92"))
                .andExpect(jsonPath("$.version").value(2));
    }
}
