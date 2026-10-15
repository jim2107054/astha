package com.astha.risk;

import com.astha.risk.domain.Rule;
import com.astha.risk.domain.RuleVersion;
import com.astha.risk.service.RuleManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FourEyesApprovalTest {

    @Autowired
    private RuleManagementService ruleManagementService;

    @Test
    @DisplayName("RSK-4 & CAS-5: Server enforces four-eyes policy; author cannot approve their own rule change")
    void shouldEnforceFourEyesPolicy() {
        String ruleId = "R01_PASS_THROUGH_MULE";
        String author = "risk-analyst-shakib";
        String approver = "risk-manager-tamim";

        // 1. Author drafts a new version
        RuleVersion draft = ruleManagementService.draftNewVersion(ruleId, "{\"minDistinctSenders1h\": 4}", author);
        assertThat(draft.getStatus()).isEqualTo("PENDING_APPROVAL");

        // 2. Author attempts self-approval -> MUST FAIL
        assertThatThrownBy(() -> ruleManagementService.approveVersion(draft.getId(), author, "Attempt self-approval"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Four-eyes violation");

        // 3. Different user (Risk Manager) approves -> SUCCEEDS
        RuleVersion approved = ruleManagementService.approveVersion(draft.getId(), approver, "Approved for testing");
        assertThat(approved.getStatus()).isEqualTo("ACTIVE");

        // 4. Verify rule now points to the new active version
        Rule rule = ruleManagementService.getRule(ruleId).orElseThrow();
        assertThat(rule.getActiveVersionId()).isEqualTo(draft.getId());

        // 5. Rollback to original v1 version
        Rule rolledBack = ruleManagementService.rollbackRule(ruleId, "R01_v1", approver);
        assertThat(rolledBack.getActiveVersionId()).isEqualTo("R01_v1");
    }
}
