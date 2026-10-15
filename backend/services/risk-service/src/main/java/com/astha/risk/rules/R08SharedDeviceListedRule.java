package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R08SharedDeviceListedRule implements RiskRule {
    public static final String RULE_ID = "R08_SHARED_DEVICE_LISTED";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        boolean hit = features.isBlocklisted() || features.isRingMember();

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                60,
                "R08_BLOCKLISTED_OR_RING_ASSOCIATION",
                "Transaction involves an entity or device directly linked to a confirmed fraud ring or blocklist",
                "প্রতারক চক্র বা নিষিদ্ধ তালিকাভুক্ত ডিভাইসের সাথে প্রত্যক্ষ সংযোগ বিদ্যমান"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
