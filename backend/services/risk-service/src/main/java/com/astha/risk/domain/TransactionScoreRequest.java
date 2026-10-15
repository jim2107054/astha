package com.astha.risk.domain;

import com.astha.risk.features.FeatureSnapshot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionScoreRequest {

    @NotBlank
    private String eventId;

    @NotBlank
    private String accountId;

    private String counterpartyId;

    @NotNull
    @Positive
    private Long amountPaisa;

    @NotBlank
    private String eventType; // P2P, CASH_IN, CASH_OUT, MERCHANT_QR, BILL, TOPUP, REFUND

    private String channel;

    private String deviceId;

    private String agentId;

    private String merchantId;

    private String status = "SUCCESS";

    private String traceId;

    // Optional client-supplied feature hints for testing and fast evaluation
    private FeatureSnapshot featureOverrides;

    public TransactionScoreRequest() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCounterpartyId() { return counterpartyId; }
    public void setCounterpartyId(String counterpartyId) { this.counterpartyId = counterpartyId; }

    public Long getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(Long amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public FeatureSnapshot getFeatureOverrides() { return featureOverrides; }
    public void setFeatureOverrides(FeatureSnapshot featureOverrides) { this.featureOverrides = featureOverrides; }
}
