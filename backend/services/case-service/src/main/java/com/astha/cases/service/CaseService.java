package com.astha.cases.service;

import com.astha.cases.domain.ApprovalRequest;
import com.astha.cases.domain.CaseAction;
import com.astha.cases.domain.CaseAlert;
import com.astha.cases.domain.FraudCase;
import com.astha.cases.repository.ApprovalRequestRepository;
import com.astha.cases.repository.CaseActionRepository;
import com.astha.cases.repository.CaseAlertRepository;
import com.astha.cases.repository.FraudCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final FraudCaseRepository caseRepository;
    private final CaseAlertRepository alertRepository;
    private final CaseActionRepository actionRepository;
    private final ApprovalRequestRepository approvalRepository;

    public CaseService(FraudCaseRepository caseRepository,
                       CaseAlertRepository alertRepository,
                       CaseActionRepository actionRepository,
                       ApprovalRequestRepository approvalRepository) {
        this.caseRepository = caseRepository;
        this.alertRepository = alertRepository;
        this.actionRepository = actionRepository;
        this.approvalRepository = approvalRepository;
    }

    @Transactional
    public FraudCase ingestAlert(String entityType, String entityId, String ruleId, String ruleName,
                                 int riskScore, Long amountPaisa, String eventId) {
        Long amount = amountPaisa != null ? amountPaisa : 0L;

        // Group into existing active case if present
        Optional<FraudCase> existingCaseOpt = caseRepository.findByEntityIdAndStatusNot(entityId, "CLOSED");
        FraudCase fraudCase;

        if (existingCaseOpt.isPresent()) {
            fraudCase = existingCaseOpt.get();
            fraudCase.setAlertCount(fraudCase.getAlertCount() + 1);
            fraudCase.setAmountAtRiskPaisa(fraudCase.getAmountAtRiskPaisa() + amount);
            fraudCase.setMaxRiskScore(Math.max(fraudCase.getMaxRiskScore(), riskScore));
            fraudCase.setTriageScore(calculateTriageScore(fraudCase.getMaxRiskScore(), fraudCase.getAmountAtRiskPaisa(), fraudCase.getAlertCount()));
            fraudCase.setPriority(determinePriority(fraudCase.getTriageScore()));
            fraudCase.setUpdatedAt(Instant.now());
            fraudCase = caseRepository.save(fraudCase);

            actionRepository.save(new CaseAction(UUID.randomUUID().toString(), fraudCase.getCaseId(),
                    "SYSTEM", "ALERT_GROUPED", "Grouped alert from rule: " + ruleId));
        } else {
            String caseId = "CAS-" + UUID.randomUUID().toString().substring(0, 8);
            double triage = calculateTriageScore(riskScore, amount, 1);
            fraudCase = new FraudCase(
                    caseId,
                    "Alert on " + entityType + ": " + entityId,
                    entityType,
                    entityId,
                    "OPEN",
                    determinePriority(triage),
                    triage,
                    amount,
                    riskScore
            );
            fraudCase = caseRepository.save(fraudCase);

            actionRepository.save(new CaseAction(UUID.randomUUID().toString(), caseId,
                    "SYSTEM", "CREATED", "Created case from rule: " + ruleId));
        }

        CaseAlert alert = new CaseAlert(
                UUID.randomUUID().toString(),
                fraudCase.getCaseId(),
                ruleId,
                ruleName,
                riskScore,
                amount,
                eventId
        );
        alertRepository.save(alert);

        return fraudCase;
    }

    private double calculateTriageScore(int maxRiskScore, Long amountPaisa, int alertCount) {
        // Triage formula: (Risk Score * 0.5) + (Scaled Amount * 0.3) + (Alert Count * 3)
        double amountScore = Math.min(100.0, (amountPaisa / 100_000.0) * 10.0);
        double countScore = Math.min(30.0, alertCount * 3.0);
        return Math.round(((maxRiskScore * 0.5) + (amountScore * 0.3) + countScore) * 100.0) / 100.0;
    }

    private String determinePriority(double triageScore) {
        if (triageScore >= 80.0) return "CRITICAL";
        if (triageScore >= 60.0) return "HIGH";
        if (triageScore >= 40.0) return "MEDIUM";
        return "LOW";
    }

    public List<FraudCase> getTriageQueue() {
        return caseRepository.findAllByOrderByTriageScoreDesc();
    }

    public FraudCase getCase(String caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found: " + caseId));
    }

    @Transactional
    public FraudCase assignCase(String caseId, String assigneeId, String actorId) {
        FraudCase fc = getCase(caseId);
        fc.setAssigneeId(assigneeId);
        fc.setStatus("INVESTIGATING");
        if (fc.getFirstTouchAt() == null) {
            fc.setFirstTouchAt(Instant.now());
        }
        fc.setUpdatedAt(Instant.now());
        actionRepository.save(new CaseAction(UUID.randomUUID().toString(), caseId, actorId, "ASSIGN", "Assigned to " + assigneeId));
        return caseRepository.save(fc);
    }

    @Transactional
    public CaseAction addComment(String caseId, String note, String actorId) {
        getCase(caseId);
        CaseAction action = new CaseAction(UUID.randomUUID().toString(), caseId, actorId, "COMMENT", note);
        return actionRepository.save(action);
    }

    @Transactional
    public FraudCase labelCase(String caseId, String label, String reason, String actorId) {
        FraudCase fc = getCase(caseId);
        fc.setLabel(label);
        fc.setStatus("RESOLVED");
        fc.setCloseReason(reason);
        fc.setUpdatedAt(Instant.now());
        actionRepository.save(new CaseAction(UUID.randomUUID().toString(), caseId, actorId, "LABEL", "Disposition: " + label + " - " + reason));
        return caseRepository.save(fc);
    }

    @Transactional
    public ApprovalRequest requestApproval(String caseId, String actionType, Long amountPaisa, String reason, String requesterId) {
        FraudCase fc = getCase(caseId);
        fc.setStatus("PENDING_APPROVAL");
        caseRepository.save(fc);

        String approvalId = "APP-" + UUID.randomUUID().toString().substring(0, 8);
        ApprovalRequest req = new ApprovalRequest(approvalId, caseId, requesterId, actionType, amountPaisa, reason);
        actionRepository.save(new CaseAction(UUID.randomUUID().toString(), caseId, requesterId, "APPROVAL_REQUESTED",
                "Approval requested for " + actionType + " by " + requesterId));
        return approvalRepository.save(req);
    }

    @Transactional
    public ApprovalRequest resolveApproval(String approvalId, boolean approved, String decisionNote, String approverId) {
        ApprovalRequest req = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found: " + approvalId));

        // Enforce Four-Eyes Principle: Requester cannot approve their own action
        if (req.getRequesterId().equalsIgnoreCase(approverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Four-Eyes Violation: Requester cannot approve their own action");
        }

        req.setApproverId(approverId);
        req.setStatus(approved ? "APPROVED" : "REJECTED");
        req.setDecisionNote(decisionNote);
        req.setResolvedAt(Instant.now());

        FraudCase fc = getCase(req.getCaseId());
        fc.setStatus(approved ? "RESOLVED" : "INVESTIGATING");
        caseRepository.save(fc);

        actionRepository.save(new CaseAction(UUID.randomUUID().toString(), req.getCaseId(), approverId,
                approved ? "APPROVED" : "REJECTED",
                "Action " + req.getActionType() + " was " + req.getStatus() + " by " + approverId + ": " + decisionNote));

        return approvalRepository.save(req);
    }

    public CaseReport exportCaseReport(String caseId) {
        FraudCase fc = getCase(caseId);
        List<CaseAlert> alerts = alertRepository.findByCaseId(caseId);
        List<CaseAction> actions = actionRepository.findByCaseIdOrderByTimestampAsc(caseId);

        return new CaseReport(
                fc.getCaseId(),
                fc.getTitle(),
                fc.getEntityId(),
                fc.getStatus(),
                fc.getPriority(),
                fc.getTriageScore(),
                fc.getAmountAtRiskPaisa(),
                fc.getLabel(),
                fc.getCreatedAt(),
                alerts,
                actions
        );
    }

    public record CaseReport(
            String caseId,
            String title,
            String entityId,
            String status,
            String priority,
            double triageScore,
            Long amountAtRiskPaisa,
            String label,
            Instant createdAt,
            List<CaseAlert> alerts,
            List<CaseAction> timeline
    ) {}
}
