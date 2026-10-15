package com.astha.risk.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "decision_reasons")
public class DecisionReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "rule_id", length = 64, nullable = false)
    private String ruleId;

    @Column(name = "reason_code", length = 64, nullable = false)
    private String reasonCode;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "text_en", columnDefinition = "TEXT", nullable = false)
    private String textEn;

    @Column(name = "text_bn", columnDefinition = "TEXT", nullable = false)
    private String textBn;

    public DecisionReason() {}

    public DecisionReason(UUID decisionId, String ruleId, String reasonCode, int weight, String textEn, String textBn) {
        this.decisionId = decisionId;
        this.ruleId = ruleId;
        this.reasonCode = reasonCode;
        this.weight = weight;
        this.textEn = textEn;
        this.textBn = textBn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getTextEn() { return textEn; }
    public void setTextEn(String textEn) { this.textEn = textEn; }

    public String getTextBn() { return textBn; }
    public void setTextBn(String textBn) { this.textBn = textBn; }
}
