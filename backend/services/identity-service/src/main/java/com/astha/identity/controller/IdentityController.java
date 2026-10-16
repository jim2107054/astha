package com.astha.identity.controller;

import com.astha.identity.domain.AuditLogEntry;
import com.astha.identity.domain.SystemConfig;
import com.astha.identity.service.IdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/auth/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> req) {
        String phone = req.get("phone");
        return ResponseEntity.ok(Map.of("message", identityService.sendOtp(phone)));
    }

    @PostMapping("/auth/otp/verify")
    public ResponseEntity<IdentityService.AuthResult> verifyOtp(@RequestBody Map<String, String> req) {
        String phone = req.get("phone");
        String otp = req.get("otp");
        return ResponseEntity.ok(identityService.verifyOtp(phone, otp));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<IdentityService.AuthResult> loginStaff(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String role = req.getOrDefault("role", "ANALYST");
        return ResponseEntity.ok(identityService.loginStaff(email, role));
    }

    @GetMapping("/audit/logs")
    public ResponseEntity<List<AuditLogEntry>> getAuditLogs() {
        return ResponseEntity.ok(identityService.getAuditLogs());
    }

    @PostMapping("/audit/reveal-pii")
    public ResponseEntity<Map<String, String>> revealPii(
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "analyst-01") String actorId,
            @RequestHeader(value = "X-Forwarded-For", defaultValue = "127.0.0.1") String ip) {
        String resourceType = req.getOrDefault("resourceType", "ACCOUNT");
        String resourceId = req.get("resourceId");
        String reason = req.get("reason");
        String masked = req.get("maskedValue");
        String actual = req.getOrDefault("actualValue", "+8801712345678");

        String revealed = identityService.revealPiiWithReason(actorId, ip, resourceType, resourceId, reason, masked, actual);
        return ResponseEntity.ok(Map.of("revealedValue", revealed));
    }

    @GetMapping("/admin/configs")
    public ResponseEntity<List<SystemConfig>> getConfigs() {
        return ResponseEntity.ok(identityService.getAllConfigs());
    }

    @PutMapping("/admin/configs/{key}")
    public ResponseEntity<SystemConfig> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "admin-01") String actorId) {
        String value = req.get("value");
        return ResponseEntity.ok(identityService.updateConfig(key, value, actorId));
    }
}
