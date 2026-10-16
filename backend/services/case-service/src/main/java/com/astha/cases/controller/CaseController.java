package com.astha.cases.controller;

import com.astha.cases.domain.ApprovalRequest;
import com.astha.cases.domain.CaseAction;
import com.astha.cases.domain.FraudCase;
import com.astha.cases.service.CaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public ResponseEntity<List<FraudCase>> getTriageQueue() {
        return ResponseEntity.ok(caseService.getTriageQueue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FraudCase> getCase(@PathVariable String id) {
        return ResponseEntity.ok(caseService.getCase(id));
    }

    @PostMapping("/alerts")
    public ResponseEntity<FraudCase> ingestAlert(@RequestBody Map<String, Object> req) {
        String entityType = (String) req.getOrDefault("entityType", "ACCOUNT");
        String entityId = (String) req.get("entityId");
        String ruleId = (String) req.get("ruleId");
        String ruleName = (String) req.getOrDefault("ruleName", "Risk Rule Hit");
        int riskScore = req.get("riskScore") instanceof Number n ? n.intValue() : 75;
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;
        String eventId = (String) req.getOrDefault("eventId", "EVT-AUTO");

        return ResponseEntity.ok(caseService.ingestAlert(entityType, entityId, ruleId, ruleName, riskScore, amount, eventId));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<FraudCase> assignCase(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "analyst-01") String actorId) {
        return ResponseEntity.ok(caseService.assignCase(id, req.get("assigneeId"), actorId));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CaseAction> addComment(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "analyst-01") String actorId) {
        return ResponseEntity.ok(caseService.addComment(id, req.get("note"), actorId));
    }

    @PostMapping("/{id}/label")
    public ResponseEntity<FraudCase> labelCase(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "analyst-01") String actorId) {
        return ResponseEntity.ok(caseService.labelCase(id, req.get("label"), req.get("reason"), actorId));
    }

    @PostMapping("/{id}/approvals")
    public ResponseEntity<ApprovalRequest> requestApproval(
            @PathVariable String id,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "analyst-01") String requesterId) {
        String actionType = (String) req.get("actionType");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;
        String reason = (String) req.get("reason");

        return ResponseEntity.ok(caseService.requestApproval(id, actionType, amount, reason, requesterId));
    }

    @PostMapping("/approvals/{approvalId}/resolve")
    public ResponseEntity<ApprovalRequest> resolveApproval(
            @PathVariable String approvalId,
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "manager-01") String approverId) {
        boolean approved = Boolean.TRUE.equals(req.get("approved"));
        String note = (String) req.getOrDefault("decisionNote", "Resolved by approver");

        return ResponseEntity.ok(caseService.resolveApproval(approvalId, approved, note, approverId));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<CaseService.CaseReport> exportReport(@PathVariable String id) {
        return ResponseEntity.ok(caseService.exportCaseReport(id));
    }
}
