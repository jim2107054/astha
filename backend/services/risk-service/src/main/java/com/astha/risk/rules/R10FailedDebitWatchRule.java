package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R10FailedDebitWatchRule implements RiskRule {
    public static final String RULE_ID = "R10_FAILED_DEBIT_WATCH";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int minAgeMinutes = parameters != null && parameters.has("minAgeMinutes") ? parameters.get("minAgeMinutes").asInt() : 10;

        boolean isFailedOrPending = "FAILED".equalsIgnoreCase(request.getStatus()) ||
                                    "PENDING".equalsIgnoreCase(request.getStatus()) ||
                                    "FAILED".equalsIgnoreCase(features.getTxStatus());

        boolean hit = isFailedOrPending && features.getAgeMinutes() >= minAgeMinutes;

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                15,
                "R10_DEBITED_BUT_FAILED_WATCH",
                "Transaction status indicates debited-but-failed anomaly eligible for auto-refund inspection",
                "টাকা কেটে নেওয়া হলেও লেনদেন সফল না হওয়ায় স্বয়ংক্রিয় রিফান্ড নিরীক্ষার আওতায় আনা হয়েছে"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
