package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R05StructuringRule implements RiskRule {
    public static final String RULE_ID = "R05_STRUCTURING";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int thresholdCount = parameters != null && parameters.has("nearLimitCount24h") ? parameters.get("nearLimitCount24h").asInt() : 3;
        long nearLimitThreshold = parameters != null && parameters.has("nearLimitThresholdPaisa") ? parameters.get("nearLimitThresholdPaisa").asLong() : 2400000L;
        long singleLimit = parameters != null && parameters.has("singleLimitPaisa") ? parameters.get("singleLimitPaisa").asLong() : 2500000L;

        boolean isCurrentNearLimit = request.getAmountPaisa() != null &&
                                     request.getAmountPaisa() >= nearLimitThreshold &&
                                     request.getAmountPaisa() <= singleLimit;

        int totalCount = features.getNearLimitCount24h() + (isCurrentNearLimit ? 1 : 0);

        if (totalCount >= thresholdCount) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                25,
                "R05_STRUCTURING_NEAR_LIMIT",
                "Transaction structuring detected: repeated transactions just below regulatory reporting thresholds",
                "লেনদেন সীমাবদ্ধতা এড়াতে নির্ধারিত সীমার ঠিক নিচে বারবার একাধিক লেনদেন"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
