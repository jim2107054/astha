package com.astha.risk.controller;

import com.astha.risk.domain.Rule;
import com.astha.risk.domain.RuleVersion;
import com.astha.risk.service.RuleManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/risk")
public class RuleController {

    private final RuleManagementService ruleManagementService;

    public RuleController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<Rule>> getRules() {
        return ResponseEntity.ok(ruleManagementService.getAllRules());
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<Rule> getRule(@PathVariable String id) {
        return ruleManagementService.getRule(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rules/{id}/versions")
    public ResponseEntity<List<RuleVersion>> getRuleVersions(@PathVariable String id) {
        return ResponseEntity.ok(ruleManagementService.getRuleVersions(id));
    }

    @PostMapping("/rules/{id}/versions")
    public ResponseEntity<RuleVersion> draftVersion(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-User-Id", defaultValue = "analyst-1") String userId) {
        String params = payload.getOrDefault("parametersJson", "{}");
        RuleVersion version = ruleManagementService.draftNewVersion(id, params, userId);
        return ResponseEntity.ok(version);
    }

    @PostMapping("/rules/versions/{versionId}/approve")
    public ResponseEntity<RuleVersion> approveVersion(
            @PathVariable String versionId,
            @RequestBody(required = false) Map<String, String> payload,
            @RequestHeader(value = "X-User-Id", defaultValue = "manager-1") String approverId) {
        String notes = payload != null ? payload.getOrDefault("notes", "Approved by manager") : "Approved";
        RuleVersion version = ruleManagementService.approveVersion(versionId, approverId, notes);
        return ResponseEntity.ok(version);
    }

    @PostMapping("/rules/{id}/rollback")
    public ResponseEntity<Rule> rollbackRule(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-User-Id", defaultValue = "manager-1") String userId) {
        String targetVersionId = payload.get("targetVersionId");
        Rule rule = ruleManagementService.rollbackRule(id, targetVersionId, userId);
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/backtests")
    public ResponseEntity<Map<String, Object>> runBacktest(@RequestBody Map<String, Object> req) {
        String ruleId = (String) req.getOrDefault("ruleId", "R01_PASS_THROUGH_MULE");
        String versionId = (String) req.getOrDefault("versionId", "R01_v1");
        int sampleCount = ((Number) req.getOrDefault("sampleCount", 5000)).intValue();
        Map<String, Object> result = ruleManagementService.runBacktest(ruleId, versionId, sampleCount, 0.88, 0.92);
        return ResponseEntity.ok(result);
    }
}
