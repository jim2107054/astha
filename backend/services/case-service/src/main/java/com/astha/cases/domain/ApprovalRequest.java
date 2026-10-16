package com.astha.cases.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    private String approvalId;
    private String caseId;
    private String requesterId;
    private String approverId;
    private String actionType; // ACCOUNT_FREEZE, SAR_FILING, REJECT_DISPUTE
    private Long amountPaisa;
    private String status;     // PENDING, APPROVED, REJECTED
    private String reason;
    private String decisionNote;
    private Instant createdAt;
    private Instant resolvedAt;

    public ApprovalRequest() {}

    public ApprovalRequest(String approvalId, String caseId, String requesterId,
                           String actionType, Long amountPaisa, String reason) {
        this.approvalId = approvalId;
        this.caseId = caseId;
        this.requesterId = requesterId;
        this.actionType = actionType;
        this.amountPaisa = amountPaisa;
        this.status = "PENDING";
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public String getApprovalId() { return approvalId; }
    public void setApprovalId(String approvalId) { this.approvalId = approvalId; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Long getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(Long amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
