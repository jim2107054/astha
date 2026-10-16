package com.astha.notifications;

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
public class NotificationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("NTF-1 & NTF-2: Bilingual notification dispatch and dev inbox query")
    void shouldDispatchBilingualNotifications() throws Exception {
        // 1. Dispatch Bengali Dispute Filed Notification
        mockMvc.perform(post("/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "recipient": "+8801711223344",
                  "channel": "SMS",
                  "templateKey": "DISPUTE_FILED",
                  "language": "bn",
                  "params": {
                    "id": "DSP-909"
                  }
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.body", containsString("বাংলাদেশ ব্যাংক")));

        // 2. Dispatch English Auto-Refund Notification
        mockMvc.perform(post("/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "recipient": "+8801711223344",
                  "channel": "SMS",
                  "templateKey": "AUTO_REFUND_CREDITED",
                  "language": "en",
                  "params": {
                    "id": "TXN-8812",
                    "amount": "BDT 2,500"
                  }
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", containsString("automatically refunded")));

        // 3. Query Dev Inbox
        mockMvc.perform(get("/v1/notifications/inbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].recipient").value("+8801711223344"));
    }
}
