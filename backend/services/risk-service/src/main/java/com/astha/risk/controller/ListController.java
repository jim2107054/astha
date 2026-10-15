package com.astha.risk.controller;

import com.astha.risk.domain.ListEntity;
import com.astha.risk.service.RuleManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/risk/lists")
public class ListController {

    private final RuleManagementService ruleManagementService;

    public ListController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }

    @GetMapping
    public ResponseEntity<List<ListEntity>> getLists(@RequestParam(defaultValue = "BLOCK") String type) {
        return ResponseEntity.ok(ruleManagementService.getLists(type.toUpperCase()));
    }

    @PostMapping
    public ResponseEntity<ListEntity> addToList(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-User-Id", defaultValue = "analyst-1") String userId) {
        String listType = payload.getOrDefault("listType", "BLOCK");
        String entityType = payload.getOrDefault("entityType", "ACCOUNT");
        String entityValue = payload.get("entityValue");
        String reason = payload.getOrDefault("reason", "Manual review flag");
        String expiresAtStr = payload.get("expiresAt");
        Instant expiresAt = expiresAtStr != null ? Instant.parse(expiresAtStr) : Instant.now().plusSeconds(86400 * 30);

        ListEntity created = ruleManagementService.addToList(listType, entityType, entityValue, reason, userId, expiresAt);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFromList(@PathVariable UUID id) {
        ruleManagementService.deleteFromList(id);
        return ResponseEntity.noContent().build();
    }
}
