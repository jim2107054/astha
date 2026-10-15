package com.astha.risk;

import com.astha.risk.domain.ListEntity;
import com.astha.risk.domain.ScoreResponse;
import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.engine.RiskEngine;
import com.astha.risk.service.RuleManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ListsManagementTest {

    @Autowired
    private RiskEngine riskEngine;

    @Autowired
    private RuleManagementService ruleManagementService;

    @Test
    @DisplayName("RSK-8: Blocklist entity immediately receives BLOCK_RECOMMENDED decision")
    void shouldBlockListedAccount() {
        String accountId = "acc-fraudster-007";
        ListEntity entry = ruleManagementService.addToList(
                "BLOCK", "ACCOUNT", accountId, "Confirmed mule account from CID report", "admin", Instant.now().plusSeconds(3600)
        );

        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setEventId("evt-block-001");
        req.setAccountId(accountId);
        req.setAmountPaisa(100000L);
        req.setEventType("P2P");

        ScoreResponse response = riskEngine.score(req);
        assertThat(response.getScore()).isEqualTo(100);
        assertThat(response.getDecision()).isEqualTo("BLOCK_RECOMMENDED");
        assertThat(response.getReasonCodes())
                .extracting(ScoreResponse.ReasonCodeDetail::code)
                .contains("R08_BLOCKLISTED_OR_RING_ASSOCIATION");

        // Cleanup
        ruleManagementService.deleteFromList(entry.getId());
    }

    @Test
    @DisplayName("RSK-8: Allowlist entity immediately receives ALLOW decision")
    void shouldAllowListedAccount() {
        String accountId = "acc-vip-merchant-999";
        ListEntity entry = ruleManagementService.addToList(
                "ALLOW", "ACCOUNT", accountId, "High-volume verified merchant partner", "admin", Instant.now().plusSeconds(3600)
        );

        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setEventId("evt-allow-001");
        req.setAccountId(accountId);
        req.setAmountPaisa(50000000L); // 5 lakh Taka
        req.setEventType("P2P");

        ScoreResponse response = riskEngine.score(req);
        assertThat(response.getScore()).isEqualTo(0);
        assertThat(response.getDecision()).isEqualTo("ALLOW");
        assertThat(response.getReasonCodes())
                .extracting(ScoreResponse.ReasonCodeDetail::code)
                .contains("ALLOWLIST_EXEMPTION");

        // Cleanup
        ruleManagementService.deleteFromList(entry.getId());
    }
}
