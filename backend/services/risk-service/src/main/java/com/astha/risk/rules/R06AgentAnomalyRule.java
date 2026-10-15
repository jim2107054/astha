package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R06AgentAnomalyRule implements RiskRule {
    public static final String RULE_ID = "R06_AGENT_ANOMALY";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        double zscoreThreshold = parameters != null && parameters.has("zscoreCashoutThreshold") ? parameters.get("zscoreCashoutThreshold").asDouble() : 3.0;
        int maxCustomersPerDevice = parameters != null && parameters.has("maxCustomersPerDevice") ? parameters.get("maxCustomersPerDevice").asInt() : 5;

        boolean hit = features.getAgentCashoutVelocityZScore() >= zscoreThreshold ||
                      features.getCustomersPerDevice() > maxCustomersPerDevice;

        if (hit) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                30,
                "R06_AGENT_VELOCITY_ANOMALY",
                "Agent cash-out volume anomaly or device sharing threshold exceeded",
                "এজেন্টের ক্যাশ-আউট গতি সীমা ছাড়িয়েছে অথবা এক ডিভাইসে অতিরিক্ত গ্রাহক নিবন্ধন"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
