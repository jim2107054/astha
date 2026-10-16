package com.astha.providermock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mock_accounts")
public class MockAccount {

    @Id
    private String accountId;
    private String msisdn;
    private String accountHolderName;
    private Long balancePaisa;
    private String status; // ACTIVE, FROZEN, SUSPENDED
    private String kycTier; // TIER_1, TIER_2, TIER_3
    private String accountType; // CUSTOMER, AGENT, MERCHANT
    private String deviceFingerprint;
    private Instant createdAt;
    private Instant updatedAt;

    public MockAccount() {}

    public MockAccount(String accountId, String msisdn, String accountHolderName, Long balancePaisa,
                       String status, String kycTier, String accountType, String deviceFingerprint) {
        this.accountId = accountId;
        this.msisdn = msisdn;
        this.accountHolderName = accountHolderName;
        this.balancePaisa = balancePaisa;
        this.status = status;
        this.kycTier = kycTier;
        this.accountType = accountType;
        this.deviceFingerprint = deviceFingerprint;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public Long getBalancePaisa() { return balancePaisa; }
    public void setBalancePaisa(Long balancePaisa) { this.balancePaisa = balancePaisa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getKycTier() { return kycTier; }
    public void setKycTier(String kycTier) { this.kycTier = kycTier; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
