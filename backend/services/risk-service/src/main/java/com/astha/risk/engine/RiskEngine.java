package com.astha.risk.engine;

import com.astha.platform.outbox.OutboxPublisher;
import com.astha.risk.domain.*;
import com.astha.risk.features.FeatureSnapshot;
import com.astha.risk.features.FeatureStore;
import com.astha.risk.repository.*;
import com.astha.risk.rules.RiskRule;
import com.astha.risk.rules.RuleEvaluationResult;
import com.astha.risk.rules.RuleRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class RiskEngine {
    private static final Logger log = LoggerFactory.getLogger(RiskEngine.class);

    private final FeatureStore featureStore;
    private final RuleRegistry ruleRegistry;
    private final RuleRepository ruleRepository;
    private final RuleVersionRepository ruleVersionRepository;
    private final ListEntityRepository listEntityRepository;
    private final DecisionRepository decisionRepository;
    private final DecisionReasonRepository decisionReasonRepository;
    private final ShadowResultRepository shadowResultRepository;
    private final OutboxPublisher outboxPublisher;
    private final ObjectMapper objectMapper;

    public RiskEngine(FeatureStore featureStore,
                      RuleRegistry ruleRegistry,
                      RuleRepository ruleRepository,
                      RuleVersionRepository ruleVersionRepository,
                      ListEntityRepository listEntityRepository,
                      DecisionRepository decisionRepository,
                      DecisionReasonRepository decisionReasonRepository,
                      ShadowResultRepository shadowResultRepository,
                      OutboxPublisher outboxPublisher,
                      ObjectMapper objectMapper) {
        this.featureStore = featureStore;
        this.ruleRegistry = ruleRegistry;
        this.ruleRepository = ruleRepository;
        this.ruleVersionRepository = ruleVersionRepository;
        this.listEntityRepository = listEntityRepository;
        this.decisionRepository = decisionRepository;
        this.decisionReasonRepository = decisionReasonRepository;
        this.shadowResultRepository = shadowResultRepository;
        this.outboxPublisher = outboxPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ScoreResponse score(TransactionScoreRequest request) {
        long startNano = System.nanoTime();
        Instant now = Instant.now();
        UUID decisionId = UUID.randomUUID();

        // 1. Check Allowlist / Blocklist (RSK-8)
        Optional<ListEntity> blockHit = listEntityRepository.findByListTypeAndEntityTypeAndEntityValue("BLOCK", "ACCOUNT", request.getAccountId());
        if (blockHit.isPresent() && !blockHit.get().isExpired(now)) {
            return finalizeDecision(decisionId, request, 100, "BLOCK_RECOMMENDED",
                    List.of(new ScoreResponse.ReasonCodeDetail("R08_BLOCKLISTED_OR_RING_ASSOCIATION", "R08_SHARED_DEVICE_LISTED", 100,
                            "Account is actively blocklisted: " + blockHit.get().getReason(),
                            "অ্যাকাউন্টটি কালোতালিকাভুক্ত: " + blockHit.get().getReason())),
                    Map.of("LIST_BLOCK", "v1"), new FeatureSnapshot(), startNano, false);
        }

        Optional<ListEntity> allowHit = listEntityRepository.findByListTypeAndEntityTypeAndEntityValue("ALLOW", "ACCOUNT", request.getAccountId());
        if (allowHit.isPresent() && !allowHit.get().isExpired(now)) {
            return finalizeDecision(decisionId, request, 0, "ALLOW",
                    List.of(new ScoreResponse.ReasonCodeDetail("ALLOWLIST_EXEMPTION", "SYSTEM", 0,
                            "Account is on the active allowlist: " + allowHit.get().getReason(),
                            "অ্যাকাউন্টটি অনুমোদিত তালিকায় অন্তর্ভুক্ত")),
                    Map.of("LIST_ALLOW", "v1"), new FeatureSnapshot(), startNano, false);
        }

        // 2. Fetch Features with Degraded Fallback Check (RSK-6, NFR-5)
        boolean degraded = !featureStore.isHealthy();
        FeatureSnapshot features = featureStore.getFeatures(request);

        if (degraded) {
            log.warn("Feature store unhealthy! Executing degraded fallback decision for event {}", request.getEventId());
            ScoreResponse.ReasonCodeDetail degradedReason = new ScoreResponse.ReasonCodeDetail(
                    "DEGRADED_FEATURE_STORE_TIMEOUT", "SYSTEM", 0,
                    "Feature store timeout: decision safely defaulted to ALLOW_WITH_FLAG with review alert",
                    "ফিচার স্টোর ত্রুটি: লেনদেনটি সতর্কতামূলক ফ্ল্যাগসহ অনুমোদিত হয়েছে"
            );
            return finalizeDecision(decisionId, request, 10, "ALLOW_WITH_FLAG",
                    List.of(degradedReason), Map.of("FALLBACK", "v1"), features, startNano, true);
        }

        // 3. Evaluate Active Rules (RSK-1, RSK-3)
        List<Rule> activeRules = ruleRepository.findAll();
        List<ScoreResponse.ReasonCodeDetail> reasonDetails = new ArrayList<>();
        Map<String, String> activeRuleVersions = new HashMap<>();
        int totalScore = 0;
        boolean hasCritical = false;

        for (Rule rule : activeRules) {
            if (rule.getActiveVersionId() == null) continue;
            Optional<RuleVersion> versionOpt = ruleVersionRepository.findById(rule.getActiveVersionId());
            if (versionOpt.isEmpty()) continue;

            RuleVersion version = versionOpt.get();
            activeRuleVersions.put(rule.getId(), "v" + version.getVersionNumber());

            Optional<RiskRule> ruleImpl = ruleRegistry.getRule(rule.getId());
            if (ruleImpl.isPresent()) {
                JsonNode params = parseJsonNode(version.getParametersJson());
                RuleEvaluationResult eval = ruleImpl.get().evaluate(request, features, params);
                if (eval.hit()) {
                    totalScore += eval.scoreContribution();
                    if ("CRITICAL".equalsIgnoreCase(rule.getSeverity())) {
                        hasCritical = true;
                    }
                    reasonDetails.add(new ScoreResponse.ReasonCodeDetail(
                            eval.reasonCode(),
                            rule.getId(),
                            eval.scoreContribution(),
                            eval.textEn(),
                            eval.textBn()
                    ));
                }
            }
        }

        // 4. Score Aggregation and Decision Thresholding
        int finalScore = Math.min(100, totalScore);
        if (hasCritical && finalScore < 80) {
            finalScore = 80;
        }

        String decision;
        if (finalScore >= 65) {
            decision = "BLOCK_RECOMMENDED";
        } else if (finalScore >= 35) {
            decision = "REVIEW";
        } else {
            decision = "ALLOW";
        }

        // 5. Evaluate Shadow Candidate Versions (RSK-5)
        evaluateShadowRules(request, features);

        // Update real-time features with current event
        featureStore.updateFeatures(request);

        return finalizeDecision(decisionId, request, finalScore, decision, reasonDetails, activeRuleVersions, features, startNano, false);
    }

    private void evaluateShadowRules(TransactionScoreRequest request, FeatureSnapshot features) {
        try {
            List<RuleVersion> shadowVersions = ruleVersionRepository.findAll().stream()
                    .filter(v -> "PENDING_APPROVAL".equalsIgnoreCase(v.getStatus()) || "DRAFT".equalsIgnoreCase(v.getStatus()))
                    .toList();

            for (RuleVersion sv : shadowVersions) {
                ruleRegistry.getRule(sv.getRuleId()).ifPresent(rule -> {
                    JsonNode params = parseJsonNode(sv.getParametersJson());
                    RuleEvaluationResult eval = rule.evaluate(request, features, params);
                    ShadowResult sr = new ShadowResult(
                            request.getEventId(),
                            sv.getId(),
                            sv.getRuleId(),
                            eval.scoreContribution(),
                            eval.hit(),
                            Instant.now()
                    );
                    shadowResultRepository.save(sr);
                });
            }
        } catch (Exception e) {
            log.warn("Error evaluating shadow rules: {}", e.getMessage());
        }
    }

    private ScoreResponse finalizeDecision(UUID decisionId, TransactionScoreRequest request, int score, String decision,
                                          List<ScoreResponse.ReasonCodeDetail> reasons, Map<String, String> ruleVersions,
                                          FeatureSnapshot features, long startNano, boolean degraded) {
        int latencyMs = (int) ((System.nanoTime() - startNano) / 1_000_000);
        Instant now = Instant.now();

        String featuresJson = toJson(features);
        String ruleVersionsJson = toJson(ruleVersions);

        // Persist decision snapshot (RSK-1, NFR-6)
        Decision decisionEntity = new Decision(
                decisionId,
                request.getEventId(),
                request.getAccountId(),
                score,
                decision,
                featuresJson,
                ruleVersionsJson,
                latencyMs,
                degraded,
                now
        );
        decisionRepository.save(decisionEntity);

        // Persist reasons
        for (ScoreResponse.ReasonCodeDetail r : reasons) {
            DecisionReason dr = new DecisionReason(
                    decisionId,
                    r.ruleId(),
                    r.code(),
                    r.weight(),
                    r.textEn(),
                    r.textBn()
            );
            decisionReasonRepository.save(dr);
        }

        // Publish to outbox (transactional outbox pattern)
        Map<String, Object> decisionEvent = new LinkedHashMap<>();
        decisionEvent.put("decisionId", decisionId.toString());
        decisionEvent.put("eventId", request.getEventId());
        decisionEvent.put("accountId", request.getAccountId());
        decisionEvent.put("score", score);
        decisionEvent.put("decision", decision);
        decisionEvent.put("decidedAt", now.toString());
        decisionEvent.put("reasonCodes", reasons.stream().map(ScoreResponse.ReasonCodeDetail::code).toList());
        decisionEvent.put("ruleVersions", ruleVersions);
        decisionEvent.put("featuresSnapshot", features);
        decisionEvent.put("latencyMs", latencyMs);
        decisionEvent.put("degraded", degraded);
        outboxPublisher.publish("RiskDecision", decisionId.toString(), "RiskDecided", "risk.decisions.v1", decisionEvent, request.getTraceId());

        // If REVIEW or BLOCK_RECOMMENDED or ALLOW_WITH_FLAG, emit an alert
        if (!"ALLOW".equals(decision)) {
            Map<String, Object> alertEvent = new LinkedHashMap<>();
            alertEvent.put("alertId", UUID.randomUUID().toString());
            alertEvent.put("eventId", request.getEventId());
            alertEvent.put("source", "RISK_ENGINE");
            alertEvent.put("entityId", request.getAccountId());
            alertEvent.put("entityType", "ACCOUNT");
            alertEvent.put("typology", reasons.isEmpty() ? "SUSPICIOUS" : reasons.get(0).ruleId());
            alertEvent.put("severity", score >= 75 ? "CRITICAL" : (score >= 50 ? "HIGH" : "MEDIUM"));
            alertEvent.put("score", score);
            alertEvent.put("createdAt", now.toString());
            alertEvent.put("reasonCodes", reasons.stream().map(ScoreResponse.ReasonCodeDetail::code).toList());
            alertEvent.put("evidence", Map.of("features", features, "reasons", reasons));
            alertEvent.put("amountAtRiskPaisa", request.getAmountPaisa() != null ? request.getAmountPaisa() : 0L);
            outboxPublisher.publish("Alert", UUID.randomUUID().toString(), "AlertRaised", "alerts.v1", alertEvent, request.getTraceId());
        }

        return new ScoreResponse(
                decisionId,
                request.getEventId(),
                request.getAccountId(),
                score,
                decision,
                reasons,
                ruleVersions,
                features,
                latencyMs,
                degraded,
                now
        );
    }

    private JsonNode parseJsonNode(String json) {
        try {
            return json != null ? objectMapper.readTree(json) : objectMapper.createObjectNode();
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
