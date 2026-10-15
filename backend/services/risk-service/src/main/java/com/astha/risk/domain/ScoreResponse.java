package com.astha.risk.domain;

import com.astha.risk.features.FeatureSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreResponse {
    private UUID decisionId;
    private String eventId;
    private String accountId;
    private int score;
    private String decision; // ALLOW, REVIEW, BLOCK_RECOMMENDED, ALLOW_WITH_FLAG
    private List<ReasonCodeDetail> reasonCodes;
    private Map<String, String> ruleVersions;
    private FeatureSnapshot featuresSnapshot;
    private int latencyMs;
    private boolean degraded;
    private Instant decidedAt;

    public ScoreResponse() {}

    public ScoreResponse(UUID decisionId, String eventId, String accountId, int score, String decision,
                         List<ReasonCodeDetail> reasonCodes, Map<String, String> ruleVersions,
                         FeatureSnapshot featuresSnapshot, int latencyMs, boolean degraded, Instant decidedAt) {
        this.decisionId = decisionId;
        this.eventId = eventId;
        this.accountId = accountId;
        this.score = score;
        this.decision = decision;
        this.reasonCodes = reasonCodes;
        this.ruleVersions = ruleVersions;
        this.featuresSnapshot = featuresSnapshot;
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

    public List<ReasonCodeDetail> getReasonCodes() { return reasonCodes; }
    public void setReasonCodes(List<ReasonCodeDetail> reasonCodes) { this.reasonCodes = reasonCodes; }

    public Map<String, String> getRuleVersions() { return ruleVersions; }
    public void setRuleVersions(Map<String, String> ruleVersions) { this.ruleVersions = ruleVersions; }

    public FeatureSnapshot getFeaturesSnapshot() { return featuresSnapshot; }
    public void setFeaturesSnapshot(FeatureSnapshot featuresSnapshot) { this.featuresSnapshot = featuresSnapshot; }

    public int getLatencyMs() { return latencyMs; }
    public void setLatencyMs(int latencyMs) { this.latencyMs = latencyMs; }

    public boolean isDegraded() { return degraded; }
    public void setDegraded(boolean degraded) { this.degraded = degraded; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public record ReasonCodeDetail(String code, String ruleId, int weight, String textEn, String textBn) {}
}
