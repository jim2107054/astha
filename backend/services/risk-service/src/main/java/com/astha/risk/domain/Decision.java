package com.astha.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decisions")
public class Decision {

    @Id
    @Column(name = "decision_id")
    private UUID decisionId;

    @Column(name = "event_id", length = 128, nullable = false, unique = true)
    private String eventId;

    @Column(name = "account_id", length = 64, nullable = false)
    private String accountId;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "decision", length = 32, nullable = false)
    private String decision; // ALLOW, REVIEW, BLOCK_RECOMMENDED, ALLOW_WITH_FLAG

    @Column(name = "features_snapshot_json", columnDefinition = "TEXT", nullable = false)
    private String featuresSnapshotJson;

    @Column(name = "rule_versions_json", columnDefinition = "TEXT", nullable = false)
    private String ruleVersionsJson;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "degraded", nullable = false)
    private boolean degraded;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    public Decision() {}

    public Decision(UUID decisionId, String eventId, String accountId, int score, String decision,
                    String featuresSnapshotJson, String ruleVersionsJson, int latencyMs, boolean degraded, Instant decidedAt) {
        this.decisionId = decisionId;
        this.eventId = eventId;
        this.accountId = accountId;
        this.score = score;
        this.decision = decision;
        this.featuresSnapshotJson = featuresSnapshotJson;
        this.ruleVersionsJson = ruleVersionsJson;
        this.latencyMs = latencyMs;
        this.degraded = degraded;
        this.decidedAt = decidedAt;
    }

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getFeaturesSnapshotJson() { return featuresSnapshotJson; }
    public void setFeaturesSnapshotJson(String featuresSnapshotJson) { this.featuresSnapshotJson = featuresSnapshotJson; }

    public String getRuleVersionsJson() { return ruleVersionsJson; }
    public void setRuleVersionsJson(String ruleVersionsJson) { this.ruleVersionsJson = ruleVersionsJson; }

    public int getLatencyMs() { return latencyMs; }
    public void setLatencyMs(int latencyMs) { this.latencyMs = latencyMs; }

    public boolean isDegraded() { return degraded; }
    public void setDegraded(boolean degraded) { this.degraded = degraded; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
