package com.astha.analytics.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rule_quality_stats")
public class RuleQualityStat {

    @Id
    private String ruleId;
    private String ruleName;
    private int totalTriggerCount;
    private int trueFraudCount;
    private int falsePositiveCount;
    private double precisionRate;
    private double falsePositiveRate;

    public RuleQualityStat() {}

    public RuleQualityStat(String ruleId, String ruleName, int totalTriggerCount, int trueFraudCount, int falsePositiveCount) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.totalTriggerCount = totalTriggerCount;
        this.trueFraudCount = trueFraudCount;
        this.falsePositiveCount = falsePositiveCount;
        this.precisionRate = totalTriggerCount > 0 ? (double) trueFraudCount / totalTriggerCount : 1.0;
        this.falsePositiveRate = totalTriggerCount > 0 ? (double) falsePositiveCount / totalTriggerCount : 0.0;
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public int getTotalTriggerCount() { return totalTriggerCount; }
    public void setTotalTriggerCount(int totalTriggerCount) { this.totalTriggerCount = totalTriggerCount; }

    public int getTrueFraudCount() { return trueFraudCount; }
    public void setTrueFraudCount(int trueFraudCount) { this.trueFraudCount = trueFraudCount; }

    public int getFalsePositiveCount() { return falsePositiveCount; }
    public void setFalsePositiveCount(int falsePositiveCount) { this.falsePositiveCount = falsePositiveCount; }

    public double getPrecisionRate() { return precisionRate; }
    public void setPrecisionRate(double precisionRate) { this.precisionRate = precisionRate; }

    public double getFalsePositiveRate() { return falsePositiveRate; }
    public void setFalsePositiveRate(double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }
}
