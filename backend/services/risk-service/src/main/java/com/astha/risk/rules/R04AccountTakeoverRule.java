package com.astha.risk.rules;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class R04AccountTakeoverRule implements RiskRule {
    public static final String RULE_ID = "R04_ACCOUNT_TAKEOVER";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public RuleEvaluationResult evaluate(TransactionScoreRequest request, FeatureSnapshot features, JsonNode parameters) {
        double maxHoursSinceChange = parameters != null && parameters.has("hoursSinceSimOrDeviceChange") ? parameters.get("hoursSinceSimOrDeviceChange").asDouble() : 24.0;
        double minDrainRatio = parameters != null && parameters.has("minBalanceDrainRatio") ? parameters.get("minBalanceDrainRatio").asDouble() : 0.75;
        boolean requireNewBeneficiary = !parameters.has("requireNewBeneficiary") || parameters.get("requireNewBeneficiary").asBoolean();

        boolean condition = features.getHoursSinceSimOrDeviceChange() <= maxHoursSinceChange &&
                            features.getBalanceDrainRatio() >= minDrainRatio &&
                            (!requireNewBeneficiary || features.isNewBeneficiary());

        if (condition) {
            return RuleEvaluationResult.hit(
                RULE_ID,
                50,
                "R04_ACCOUNT_TAKEOVER_DRAIN",
                "Account takeover suspected: sudden high-value drain to new counterparty following SIM/device change",
                "হিসাব দখলের সন্দেহ: সিম বা ডিভাইস পরিবর্তনের পরপরই নতুন সুবিধাভোগীর কাছে বড় অঙ্কের তহবিল স্থানান্তর"
            );
        }
        return RuleEvaluationResult.notHit(RULE_ID);
    }
}
