package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R09RingScoreRule implements RiskRule {
    public static final String RULE_ID = "R09_RING_SCORE";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        int minScore = parameters != null && parameters.has("minRingScore") ? parameters.get("minRingScore").asInt() : 75;
        int minSize = parameters != null && parameters.has("minRingSize") ? parameters.get("minRingSize").asInt() : 3;

        boolean hit = features.getRingScore() >= minScore && features.getRingSize() >= minSize;

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                40,
                "R09_GRAPH_MULE_RING_DETECTED",
                "Graph topology analysis scored entity within an active organized mule ring",
                "গ্রাফ নেটওয়ার্ক বিশ্লেষণে সক্রিয় সংঘবদ্ধ মিউল চক্রের উপস্থিতি শনাক্ত হয়েছে"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
