package com.astha.graph.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_rings")
public class FraudRing {

    @Id
    private String ringId;
    private String typology; // MULE_CHAIN, FAN_IN_FAN_OUT, SHARED_DEVICE_CLUSTER
    private int riskScore;
    private String status; // DETECTED, REVIEWING, CONFIRMED, DISMISSED
    private int memberCount;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evidencePathJson;

    private Instant detectedAt;

    public FraudRing() {}

    public FraudRing(String ringId, String typology, int riskScore, String status, int memberCount, String evidencePathJson) {
        this.ringId = ringId;
        this.typology = typology;
        this.riskScore = riskScore;
        this.status = status;
        this.memberCount = memberCount;
        this.evidencePathJson = evidencePathJson;
        this.detectedAt = Instant.now();
    }

    public String getRingId() { return ringId; }
    public void setRingId(String ringId) { this.ringId = ringId; }

    public String getTypology() { return typology; }
    public void setTypology(String typology) { this.typology = typology; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getEvidencePathJson() { return evidencePathJson; }
    public void setEvidencePathJson(String evidencePathJson) { this.evidencePathJson = evidencePathJson; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
