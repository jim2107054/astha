package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R01PassThroughMuleRule implements RiskRule {
    public static final String RULE_ID = "R01_PASS_THROUGH_MULE";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int minSenders = parameters != null && parameters.has("minDistinctSenders1h") ? parameters.get("minDistinctSenders1h").asInt() : 3;
        long minInflow = parameters != null && parameters.has("minInflow1hPaisa") ? parameters.get("minInflow1hPaisa").asLong() : 1000000L;
        double minOutflowRatio = parameters != null && parameters.has("minOutflowRatio30m") ? parameters.get("minOutflowRatio30m").asDouble() : 0.80;
        int maxAccountAge = parameters != null && parameters.has("maxAccountAgeDays") ? parameters.get("maxAccountAgeDays").asInt() : 30;

        boolean hit = features.getDistinctSenders1h() >= minSenders &&
                      features.getInflow1hPaisa() >= minInflow &&
                      features.getOutflowRatio30m() >= minOutflowRatio &&
                      features.getAccountAgeDays() <= maxAccountAge;

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                35,
                "R01_FAST_PASS_THROUGH",
                "Rapid pass-through mule pattern: multiple unrelated senders followed by immediate fund drain",
                "দ্রুত পাস-থ্রু মিউল প্যাটার্ন: একাধিক অপরিচিত প্রেরক থেকে অর্থ প্রাপ্তির পর দ্রুত ক্যাশ-আউট"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
