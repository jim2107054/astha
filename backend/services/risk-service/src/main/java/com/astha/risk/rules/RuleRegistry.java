package com.astha.risk.rules;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RuleRegistry {
    private final Map<String, RiskRule> rulesById;

    public RuleRegistry(List<RiskRule> rules) {
        this.rulesById = rules.stream().collect(Collectors.toMap(RiskRule::getRuleId, Function.identity()));
    }

    public Optional<RiskRule> getRule(String ruleId) {
        return Optional.ofNullable(rulesById.get(ruleId));
    }

    public List<RiskRule> getAllRules() {
        return List.copyOf(rulesById.values());
    }
}
