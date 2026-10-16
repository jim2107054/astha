package com.astha.providermock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mock_transactions")
public class MockTransaction {

    @Id
    private String txnId;
    private String senderAccountId;
    private String receiverAccountId;
    private Long amountPaisa;
    private String currency;
    private String txnType; // P2P, CASH_OUT, MERCHANT_PAY, ADD_MONEY
    private String status;  // SUCCESS, FAILED, PENDING, REVERSED
    private String failureReason;
    private String idempotencyKey;
    private Instant timestamp;

    public MockTransaction() {}

    public MockTransaction(String txnId, String senderAccountId, String receiverAccountId,
                           Long amountPaisa, String currency, String txnType,
                           String status, String failureReason, String idempotencyKey, Instant timestamp) {
        this.txnId = txnId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amountPaisa = amountPaisa;
        this.currency = currency;
        this.txnType = txnType;
        this.status = status;
        this.failureReason = failureReason;
        this.idempotencyKey = idempotencyKey;
        this.timestamp = timestamp;
    }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(String senderAccountId) { this.senderAccountId = senderAccountId; }

    public String getReceiverAccountId() { return receiverAccountId; }
    public void setReceiverAccountId(String receiverAccountId) { this.receiverAccountId = receiverAccountId; }

    public Long getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(Long amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
