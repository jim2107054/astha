package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R07DormantWakeUpRule implements RiskRule {
    public static final String RULE_ID = "R07_DORMANT_WAKE_UP";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int minIdleDays = parameters != null && parameters.has("minIdleDays") ? parameters.get("minIdleDays").asInt() : 90;
        long minAmount = parameters != null && parameters.has("minAmountPaisa") ? parameters.get("minAmountPaisa").asLong() : 2000000L;
        boolean requireNewDevice = !parameters.has("requireNewDevice") || parameters.get("requireNewDevice").asBoolean();

        boolean hit = features.getIdleDays() >= minIdleDays &&
                      (request.getAmountPaisa() != null && request.getAmountPaisa() >= minAmount) &&
                      (!requireNewDevice || features.isNewDevice());

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                25,
                "R07_DORMANT_ACCOUNT_SUDDEN_ACTIVITY",
                "Long-dormant account suddenly resurrected with high-value transfer from a new device",
                "দীর্ঘদিন অব্যবহৃত অ্যাকাউন্ট হঠাৎ নতুন ডিভাইস থেকে বড় অঙ্কের লেনদেনে সক্রিয়"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
