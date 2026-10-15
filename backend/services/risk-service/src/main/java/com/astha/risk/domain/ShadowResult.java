package com.astha.risk.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shadow_results")
public class ShadowResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", length = 128, nullable = false)
    private String eventId;

    @Column(name = "candidate_version_id", length = 64, nullable = false)
    private String candidateVersionId;

    @Column(name = "rule_id", length = 64, nullable = false)
    private String ruleId;

    @Column(name = "candidate_score", nullable = false)
    private int candidateScore;

    @Column(name = "candidate_hit", nullable = false)
    private boolean candidateHit;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public ShadowResult() {}

    public ShadowResult(String eventId, String candidateVersionId, String ruleId, int candidateScore, boolean candidateHit, Instant evaluatedAt) {
        this.eventId = eventId;
        this.candidateVersionId = candidateVersionId;
        this.ruleId = ruleId;
        this.candidateScore = candidateScore;
        this.candidateHit = candidateHit;
        this.evaluatedAt = evaluatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCandidateVersionId() { return candidateVersionId; }
    public void setCandidateVersionId(String candidateVersionId) { this.candidateVersionId = candidateVersionId; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public int getCandidateScore() { return candidateScore; }
    public void setCandidateScore(int candidateScore) { this.candidateScore = candidateScore; }

    public boolean isCandidateHit() { return candidateHit; }
    public void setCandidateHit(boolean candidateHit) { this.candidateHit = candidateHit; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
