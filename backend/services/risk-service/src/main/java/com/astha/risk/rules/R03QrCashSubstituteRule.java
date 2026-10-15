package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R03QrCashSubstituteRule implements RiskRule {
    public static final String RULE_ID = "R03_QR_CASH_SUBSTITUTE";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        if (!"MERCHANT_QR".equalsIgnoreCase(request.getEventType())) {
            return RuleEvaluationResult.notHit(RULE_ID);
        }

        double minRoundRatio = parameters != null && parameters.has("minRoundAmountRatio") ? parameters.get("minRoundAmountRatio").asDouble() : 0.70;
        double minRepeatRate = parameters != null && parameters.has("minPayerRepeatRate") ? parameters.get("minPayerRepeatRate").asDouble() : 0.30;
        double minNearLimitRatio = parameters != null && parameters.has("minNearLimitRatio") ? parameters.get("minNearLimitRatio").asDouble() : 0.50;

        boolean hit = features.getRoundAmountRatio() >= minRoundRatio &&
                      features.getPayerRepeatRate() >= minRepeatRate &&
                      features.getNearLimitRatio() >= minNearLimitRatio;

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                45,
                "R03_BANGLA_QR_CASH_SUBSTITUTE",
                "Bangla QR merchant abused as cash-out channel with repetitive round amounts near daily limit",
                "দৈনিক সীমার কাছাকাছি রাউন্ড ফিগারে কিউআর পেমেন্ট গ্রহণ করে ক্যাশ-আউট সুবিধা দেওয়ার সন্দেহ"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
