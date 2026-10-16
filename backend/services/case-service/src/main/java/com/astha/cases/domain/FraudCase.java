package com.astha.cases.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "fraud_cases")
public class FraudCase {

    @Id
    private String caseId;
    private String title;
    private String entityType; // ACCOUNT, DEVICE, RING, MERCHANT
    private String entityId;
    private String status;     // OPEN, INVESTIGATING, PENDING_APPROVAL, RESOLVED, CLOSED
    private String priority;   // CRITICAL, HIGH, MEDIUM, LOW
    private double triageScore;
    private Long amountAtRiskPaisa;
    private int maxRiskScore;
    private int alertCount;
    private String assigneeId;
    private String label;      // TRUE_FRAUD, FALSE_POSITIVE, UNCLEAR
    private String closeReason;
    private Instant firstTouchAt;
    private Instant createdAt;
    private Instant updatedAt;

    public FraudCase() {}

    public FraudCase(String caseId, String title, String entityType, String entityId,
                     String status, String priority, double triageScore, Long amountAtRiskPaisa,
                     int maxRiskScore) {
        this.caseId = caseId;
        this.title = title;
        this.entityType = entityType;
        this.entityId = entityId;
        this.status = status;
        this.priority = priority;
        this.triageScore = triageScore;
        this.amountAtRiskPaisa = amountAtRiskPaisa;
        this.maxRiskScore = maxRiskScore;
        this.alertCount = 1;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public double getTriageScore() { return triageScore; }
    public void setTriageScore(double triageScore) { this.triageScore = triageScore; }

    public Long getAmountAtRiskPaisa() { return amountAtRiskPaisa; }
    public void setAmountAtRiskPaisa(Long amountAtRiskPaisa) { this.amountAtRiskPaisa = amountAtRiskPaisa; }

    public int getMaxRiskScore() { return maxRiskScore; }
    public void setMaxRiskScore(int maxRiskScore) { this.maxRiskScore = maxRiskScore; }

    public int getAlertCount() { return alertCount; }
    public void setAlertCount(int alertCount) { this.alertCount = alertCount; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String closeReason) { this.closeReason = closeReason; }

    public Instant getFirstTouchAt() { return firstTouchAt; }
    public void setFirstTouchAt(Instant firstTouchAt) { this.firstTouchAt = firstTouchAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
