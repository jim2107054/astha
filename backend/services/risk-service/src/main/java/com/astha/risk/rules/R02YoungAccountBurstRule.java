package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R02YoungAccountBurstRule implements RiskRule {
    public static final String RULE_ID = "R02_YOUNG_ACCOUNT_BURST";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int maxAccountAge = parameters != null && parameters.has("maxAccountAgeDays") ? parameters.get("maxAccountAgeDays").asInt() : 7;
        long minInflow = parameters != null && parameters.has("minInflow24hPaisa") ? parameters.get("minInflow24hPaisa").asLong() : 2500000L;

        boolean hit = features.getAccountAgeDays() <= maxAccountAge &&
                      (features.getInflow24hPaisa() >= minInflow || (request.getAmountPaisa() != null && request.getAmountPaisa() >= minInflow));

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                30,
                "R02_YOUNG_ACCOUNT_BURST",
                "New account under 7 days old received sudden high-value velocity inflow",
                "৭ দিনের কম বয়সী নতুন অ্যাকাউন্টে হঠাৎ অস্বাভাবিক বড় অঙ্কের লেনদেন"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
