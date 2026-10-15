package com.astha.risk.service;

import com.astha.risk.domain.ListEntity;
import com.astha.risk.domain.Rule;
import com.astha.risk.domain.RuleApproval;
import com.astha.risk.domain.RuleVersion;
import com.astha.risk.repository.ListEntityRepository;
import com.astha.risk.repository.RuleApprovalRepository;
import com.astha.risk.repository.RuleRepository;
import com.astha.risk.repository.RuleVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RuleManagementService {

    private final RuleRepository ruleRepository;
    private final RuleVersionRepository ruleVersionRepository;
    private final RuleApprovalRepository ruleApprovalRepository;
    private final ListEntityRepository listEntityRepository;

    public RuleManagementService(RuleRepository ruleRepository,
                                 RuleVersionRepository ruleVersionRepository,
                                 RuleApprovalRepository ruleApprovalRepository,
                                 ListEntityRepository listEntityRepository) {
        this.ruleRepository = ruleRepository;
        this.ruleVersionRepository = ruleVersionRepository;
        this.ruleApprovalRepository = ruleApprovalRepository;
        this.listEntityRepository = listEntityRepository;
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    public Optional<Rule> getRule(String ruleId) {
        return ruleRepository.findById(ruleId);
    }

    public List<RuleVersion> getRuleVersions(String ruleId) {
        return ruleVersionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId);
    }

    @Transactional
    public RuleVersion draftNewVersion(String ruleId, String parametersJson, String authorId) {
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        List<RuleVersion> existingVersions = ruleVersionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId);
        int nextVersion = existingVersions.isEmpty() ? 1 : existingVersions.get(0).getVersionNumber() + 1;
        String versionId = ruleId + "_v" + nextVersion;

        RuleVersion draft = new RuleVersion(versionId, ruleId, nextVersion, "PENDING_APPROVAL", parametersJson, authorId);
        return ruleVersionRepository.save(draft);
    }

    @Transactional
    public RuleVersion approveVersion(String versionId, String approverId, String notes) {
        RuleVersion version = ruleVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Rule version not found: " + versionId));

        // Enforce Server-Side Four-Eyes Check (Rule author cannot approve own version)
        if (version.getAuthorId() != null && version.getAuthorId().equalsIgnoreCase(approverId)) {
            throw new SecurityException("Four-eyes violation: Author '" + version.getAuthorId() + "' cannot approve their own rule changes");
        }

        version.setStatus("ACTIVE");
        version.setApprovedBy(approverId);
        version.setApprovedAt(Instant.now());
        version.setApprovalNotes(notes);
        ruleVersionRepository.save(version);

        // Archive previously active versions
        List<RuleVersion> all = ruleVersionRepository.findByRuleIdOrderByVersionNumberDesc(version.getRuleId());
        for (RuleVersion v : all) {
            if (!v.getId().equals(version.getId()) && "ACTIVE".equals(v.getStatus())) {
                v.setStatus("ARCHIVED");
                ruleVersionRepository.save(v);
            }
        }

        // Update active version in Rule
        Rule rule = ruleRepository.findById(version.getRuleId()).orElseThrow();
        rule.setActiveVersionId(version.getId());
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);

        // Log approval audit
        RuleApproval approval = new RuleApproval(versionId, version.getAuthorId(), approverId, "APPROVED", notes);
        ruleApprovalRepository.save(approval);

        return version;
    }

    @Transactional
    public Rule rollbackRule(String ruleId, String targetVersionId, String requesterId) {
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        RuleVersion target = ruleVersionRepository.findById(targetVersionId)
                .orElseThrow(() -> new IllegalArgumentException("Target version not found: " + targetVersionId));

        if (!target.getRuleId().equals(ruleId)) {
            throw new IllegalArgumentException("Target version does not belong to rule: " + ruleId);
        }

        // Set previous active to archived and target to active
        List<RuleVersion> all = ruleVersionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId);
        for (RuleVersion v : all) {
            if (v.getId().equals(targetVersionId)) {
                v.setStatus("ACTIVE");
            } else if ("ACTIVE".equals(v.getStatus())) {
                v.setStatus("ARCHIVED");
            }
            ruleVersionRepository.save(v);
        }

        rule.setActiveVersionId(targetVersionId);
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);

        RuleApproval rollbackLog = new RuleApproval(targetVersionId, requesterId, requesterId, "ROLLED_BACK", "Rollback executed");
        ruleApprovalRepository.save(rollbackLog);

        return rule;
    }

    // Allowlist / Blocklist Management (RSK-8)
    public List<ListEntity> getLists(String listType) {
        return listEntityRepository.findByListTypeAndExpiresAtAfterOrExpiresAtIsNull(listType, Instant.now());
    }

    @Transactional
    public ListEntity addToList(String listType, String entityType, String entityValue, String reason, String createdBy, Instant expiresAt) {
        ListEntity entity = new ListEntity(listType, entityType, entityValue, reason, createdBy, expiresAt);
        return listEntityRepository.save(entity);
    }

    @Transactional
    public void deleteFromList(UUID id) {
        listEntityRepository.deleteById(id);
    }

    // Backtest calculation (RSK-7)
    public Map<String, Object> runBacktest(String ruleId, String versionId, int totalSampleCount, double simulatedPrecision, double simulatedRecall) {
        int alertCount = (int) (totalSampleCount * 0.05); // ~5% hit rate
        return Map.of(
                "ruleId", ruleId,
                "versionId", versionId,
                "sampleCount", totalSampleCount,
                "alertsEvaluated", alertCount,
                "precision", simulatedPrecision,
                "recall", simulatedRecall,
                "timestamp", Instant.now().toString()
        );
    }
}
