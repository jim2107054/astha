package com.astha.cases.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "case_alerts")
public class CaseAlert {

    @Id
    private String alertId;
    private String caseId;
    private String ruleId;
    private String ruleName;
    private int riskScore;
    private Long amountPaisa;
    private String eventId;
    private Instant timestamp;

    public CaseAlert() {}

    public CaseAlert(String alertId, String caseId, String ruleId, String ruleName,
                     int riskScore, Long amountPaisa, String eventId) {
        this.alertId = alertId;
        this.caseId = caseId;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.riskScore = riskScore;
        this.amountPaisa = amountPaisa;
        this.eventId = eventId;
        this.timestamp = Instant.now();
    }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public Long getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(Long amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
