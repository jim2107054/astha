package com.astha.disputes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    private String disputeId;
    private String txnId;
    private String senderAccountId;
    private String counterpartyAccountId;
    private String disputeType; // FAILED_TXN, WRONG_TRANSFER, MERCHANT_DISPUTE, UNAUTHORIZED, QR_TAMPERING
    private Long amountPaisa;
    private String status;      // SUBMITTED, UNDER_INVESTIGATION, EVIDENCE_REQUESTED, MERCHANT_PENDING, RESOLVED_REFUNDED, RESOLVED_REJECTED, ESCALATED
    private String filingSource; // CUSTOMER, OFFICER, AUTO_REFUND
    private String resolutionOutcome; // REFUND_FULL, REFUND_PARTIAL, REJECTED, CHARGEBACK
    private String resolutionNote;
    private LocalDateTime deadlineUtc;
    private String warningLevel; // NORMAL, WARNING_50, WARNING_80, BREACHED
    private String linkedCaseId;
    private Instant createdAt;
    private Instant updatedAt;

    public Dispute() {}

    public Dispute(String disputeId, String txnId, String senderAccountId, String counterpartyAccountId,
                   String disputeType, Long amountPaisa, String status, String filingSource,
                   LocalDateTime deadlineUtc) {
        this.disputeId = disputeId;
        this.txnId = txnId;
        this.senderAccountId = senderAccountId;
        this.counterpartyAccountId = counterpartyAccountId;
        this.disputeType = disputeType;
        this.amountPaisa = amountPaisa;
        this.status = status;
        this.filingSource = filingSource;
        this.deadlineUtc = deadlineUtc;
        this.warningLevel = "NORMAL";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getDisputeId() { return disputeId; }
    public void setDisputeId(String disputeId) { this.disputeId = disputeId; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(String senderAccountId) { this.senderAccountId = senderAccountId; }

    public String getCounterpartyAccountId() { return counterpartyAccountId; }
    public void setCounterpartyAccountId(String counterpartyAccountId) { this.counterpartyAccountId = counterpartyAccountId; }

    public String getDisputeType() { return disputeType; }
    public void setDisputeType(String disputeType) { this.disputeType = disputeType; }

    public Long getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(Long amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFilingSource() { return filingSource; }
    public void setFilingSource(String filingSource) { this.filingSource = filingSource; }

    public String getResolutionOutcome() { return resolutionOutcome; }
    public void setResolutionOutcome(String resolutionOutcome) { this.resolutionOutcome = resolutionOutcome; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public LocalDateTime getDeadlineUtc() { return deadlineUtc; }
    public void setDeadlineUtc(LocalDateTime deadlineUtc) { this.deadlineUtc = deadlineUtc; }

    public String getWarningLevel() { return warningLevel; }
    public void setWarningLevel(String warningLevel) { this.warningLevel = warningLevel; }

    public String getLinkedCaseId() { return linkedCaseId; }
    public void setLinkedCaseId(String linkedCaseId) { this.linkedCaseId = linkedCaseId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
