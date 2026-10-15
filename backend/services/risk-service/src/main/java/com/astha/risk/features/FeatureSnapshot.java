package com.astha.risk.features;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureSnapshot {
    private long inflow5mPaisa = 0;
    private long inflow1hPaisa = 0;
    private long inflow24hPaisa = 0;
    private long inflow7dPaisa = 0;
    private double outflowRatio30m = 0.0;
    private int distinctSenders1h = 0;
    private int accountAgeDays = 999;
    private String kycTier = "TIER_2";

    // Merchant QR features
    private double roundAmountRatio = 0.0;
    private double payerRepeatRate = 0.0;
    private double nearLimitRatio = 0.0;
    private double settlementRatio = 0.0;
    private long dailyInflowPaisa = 0;

    // Account Takeover features
    private double hoursSinceSimOrDeviceChange = 999.0;
    private double balanceDrainRatio = 0.0;
    private long drainAmountPaisa = 0;
    private boolean newBeneficiary = false;
    private boolean newDevice = false;

    // Structuring
    private int nearLimitCount24h = 0;

    // Agent anomaly
    private double agentCashoutVelocityZScore = 0.0;
    private int customersPerDevice = 1;
    private double agentRoundRatio = 0.0;

    // Dormant wake-up
    private int idleDays = 0;

    // Network / Graph
    private boolean blocklisted = false;
    private boolean ringMember = false;
    private int ringScore = 0;
    private int ringSize = 0;

    // Operational / Failed debit
    private String txStatus = "SUCCESS";
    private int ageMinutes = 0;

    private Map<String, Object> additionalFeatures = new HashMap<>();

    public FeatureSnapshot() {}

    public long getInflow5mPaisa() { return inflow5mPaisa; }
    public void setInflow5mPaisa(long inflow5mPaisa) { this.inflow5mPaisa = inflow5mPaisa; }

    public long getInflow1hPaisa() { return inflow1hPaisa; }
    public void setInflow1hPaisa(long inflow1hPaisa) { this.inflow1hPaisa = inflow1hPaisa; }

    public long getInflow24hPaisa() { return inflow24hPaisa; }
    public void setInflow24hPaisa(long inflow24hPaisa) { this.inflow24hPaisa = inflow24hPaisa; }

    public long getInflow7dPaisa() { return inflow7dPaisa; }
    public void setInflow7dPaisa(long inflow7dPaisa) { this.inflow7dPaisa = inflow7dPaisa; }

    public double getOutflowRatio30m() { return outflowRatio30m; }
    public void setOutflowRatio30m(double outflowRatio30m) { this.outflowRatio30m = outflowRatio30m; }

    public int getDistinctSenders1h() { return distinctSenders1h; }
    public void setDistinctSenders1h(int distinctSenders1h) { this.distinctSenders1h = distinctSenders1h; }

    public int getAccountAgeDays() { return accountAgeDays; }
    public void setAccountAgeDays(int accountAgeDays) { this.accountAgeDays = accountAgeDays; }

    public String getKycTier() { return kycTier; }
    public void setKycTier(String kycTier) { this.kycTier = kycTier; }

    public double getRoundAmountRatio() { return roundAmountRatio; }
    public void setRoundAmountRatio(double roundAmountRatio) { this.roundAmountRatio = roundAmountRatio; }

    public double getPayerRepeatRate() { return payerRepeatRate; }
    public void setPayerRepeatRate(double payerRepeatRate) { this.payerRepeatRate = payerRepeatRate; }

    public double getNearLimitRatio() { return nearLimitRatio; }
    public void setNearLimitRatio(double nearLimitRatio) { this.nearLimitRatio = nearLimitRatio; }

    public double getSettlementRatio() { return settlementRatio; }
    public void setSettlementRatio(double settlementRatio) { this.settlementRatio = settlementRatio; }

    public long getDailyInflowPaisa() { return dailyInflowPaisa; }
    public void setDailyInflowPaisa(long dailyInflowPaisa) { this.dailyInflowPaisa = dailyInflowPaisa; }

    public double getHoursSinceSimOrDeviceChange() { return hoursSinceSimOrDeviceChange; }
    public void setHoursSinceSimOrDeviceChange(double hoursSinceSimOrDeviceChange) { this.hoursSinceSimOrDeviceChange = hoursSinceSimOrDeviceChange; }

    public double getBalanceDrainRatio() { return balanceDrainRatio; }
    public void setBalanceDrainRatio(double balanceDrainRatio) { this.balanceDrainRatio = balanceDrainRatio; }

    public long getDrainAmountPaisa() { return drainAmountPaisa; }
    public void setDrainAmountPaisa(long drainAmountPaisa) { this.drainAmountPaisa = drainAmountPaisa; }

    public boolean isNewBeneficiary() { return newBeneficiary; }
    public void setNewBeneficiary(boolean newBeneficiary) { this.newBeneficiary = newBeneficiary; }

    public boolean isNewDevice() { return newDevice; }
    public void setNewDevice(boolean newDevice) { this.newDevice = newDevice; }

    public int getNearLimitCount24h() { return nearLimitCount24h; }
    public void setNearLimitCount24h(int nearLimitCount24h) { this.nearLimitCount24h = nearLimitCount24h; }

    public double getAgentCashoutVelocityZScore() { return agentCashoutVelocityZScore; }
    public void setAgentCashoutVelocityZScore(double agentCashoutVelocityZScore) { this.agentCashoutVelocityZScore = agentCashoutVelocityZScore; }

    public int getCustomersPerDevice() { return customersPerDevice; }
    public void setCustomersPerDevice(int customersPerDevice) { this.customersPerDevice = customersPerDevice; }

    public double getAgentRoundRatio() { return agentRoundRatio; }
    public void setAgentRoundRatio(double agentRoundRatio) { this.agentRoundRatio = agentRoundRatio; }

    public int getIdleDays() { return idleDays; }
    public void setIdleDays(int idleDays) { this.idleDays = idleDays; }

    public boolean isBlocklisted() { return blocklisted; }
    public void setBlocklisted(boolean blocklisted) { this.blocklisted = blocklisted; }

    public boolean isRingMember() { return ringMember; }
    public void setRingMember(boolean ringMember) { this.ringMember = ringMember; }

    public int getRingScore() { return ringScore; }
    public void setRingScore(int ringScore) { this.ringScore = ringScore; }

    public int getRingSize() { return ringSize; }
    public void setRingSize(int ringSize) { this.ringSize = ringSize; }

    public String getTxStatus() { return txStatus; }
    public void setTxStatus(String txStatus) { this.txStatus = txStatus; }

    public int getAgeMinutes() { return ageMinutes; }
    public void setAgeMinutes(int ageMinutes) { this.ageMinutes = ageMinutes; }

    public Map<String, Object> getAdditionalFeatures() { return additionalFeatures; }
    public void setAdditionalFeatures(Map<String, Object> additionalFeatures) { this.additionalFeatures = additionalFeatures; }
}
