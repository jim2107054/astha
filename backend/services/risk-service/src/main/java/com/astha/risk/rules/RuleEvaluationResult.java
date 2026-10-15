package com.astha.risk.rules;

public record RuleEvaluationResult(
    String ruleId,
    boolean hit,
    int scoreContribution,
    String reasonCode,
    String textEn,
    String textBn
) {
    public static RuleEvaluationResult notHit(String ruleId) {
        return new RuleEvaluationResult(ruleId, false, 0, null, null, null);
    }

    public static RuleEvaluationResult hit(String ruleId, int scoreContribution, String reasonCode, String textEn, String textBn) {
        return new RuleEvaluationResult(ruleId, true, scoreContribution, reasonCode, textEn, textBn);
    }
}
