package com.astha.identity.service;

import com.astha.identity.domain.AuditLogEntry;
import com.astha.identity.domain.SystemConfig;
import com.astha.identity.domain.UserAccount;
import com.astha.identity.repository.AuditLogRepository;
import com.astha.identity.repository.SystemConfigRepository;
import com.astha.identity.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class IdentityService {

    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    private final UserAccountRepository userRepository;
    private final AuditLogRepository auditRepository;
    private final SystemConfigRepository configRepository;

    // In-memory OTP storage: phone -> otp
    private final Map<String, String> otpStore = new HashMap<>();

    public IdentityService(UserAccountRepository userRepository,
                           AuditLogRepository auditRepository,
                           SystemConfigRepository configRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.configRepository = configRepository;

        // Seed default system configs if empty
        seedDefaultConfigs();
    }

    private void seedDefaultConfigs() {
        if (configRepository.count() == 0) {
            configRepository.save(new SystemConfig("sla.same_provider_working_days", "3", "SLA for same-provider disputes", 1, "SYSTEM"));
            configRepository.save(new SystemConfig("sla.overall_max_days", "30", "Maximum overall resolution window", 1, "SYSTEM"));
            configRepository.save(new SystemConfig("risk.auto_freeze_threshold", "90", "Risk score threshold to recommend freeze", 1, "SYSTEM"));
        }
    }

    @Transactional
    public UserAccount createUser(String userId, String username, String email, String phone, String role) {
        UserAccount user = new UserAccount(userId, username, email, phone, role);
        return userRepository.save(user);
    }

    public String sendOtp(String phone) {
        String otp = "123456"; // Default mock OTP for determinism in synthetic environment
        otpStore.put(phone, otp);
        log.info("Mock OTP sent to {}: {}", phone, otp);
        return "OTP sent successfully";
    }

    public AuthResult verifyOtp(String phone, String otp) {
        String expected = otpStore.get(phone);
        if (expected == null || !expected.equals(otp)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired OTP");
        }
        otpStore.remove(phone);

        UserAccount user = userRepository.findByPhone(phone).orElseGet(() ->
                createUser("USR-" + UUID.randomUUID().toString().substring(0, 8), phone, phone + "@astha.local", phone, "CUSTOMER")
        );

        String token = "mock-jwt-token-customer-" + user.getUserId();
        return new AuthResult(token, user.getUserId(), user.getRole(), user.getPhone());
    }

    public AuthResult loginStaff(String email, String role) {
        UserAccount user = userRepository.findByEmail(email).orElseGet(() ->
                createUser("STF-" + UUID.randomUUID().toString().substring(0, 8), email.split("@")[0], email, "+8801700000000", role)
        );

        String token = "mock-jwt-token-staff-" + user.getUserId();
        return new AuthResult(token, user.getUserId(), user.getRole(), user.getEmail());
    }

    @Transactional
    public AuditLogEntry logAudit(String actorId, String ip, String action, String resourceType, String resourceId, String details) {
        AuditLogEntry entry = new AuditLogEntry(
                "AUD-" + UUID.randomUUID(), actorId, ip, action, resourceType, resourceId, details
        );
        return auditRepository.save(entry);
    }

    public List<AuditLogEntry> getAuditLogs() {
        return auditRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public String revealPiiWithReason(String actorId, String ip, String resourceType, String resourceId, String reason, String maskedValue, String actualValue) {
        if (reason == null || reason.trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid business justification reason is required to reveal PII");
        }

        // Record audit entry for PII reveal
        logAudit(actorId, ip, "REVEAL_PII", resourceType, resourceId, "Reason: " + reason);

        return actualValue;
    }

    public List<SystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    @Transactional
    public SystemConfig updateConfig(String key, String value, String actorId) {
        SystemConfig config = configRepository.findById(key).orElseGet(() ->
                new SystemConfig(key, value, "Custom dynamic config", 0, actorId)
        );

        config.setConfigValue(value);
        config.setVersion(config.getVersion() + 1);
        config.setUpdatedBy(actorId);
        config.setUpdatedAt(Instant.now());

        logAudit(actorId, "127.0.0.1", "UPDATE_CONFIG", "SYSTEM_CONFIG", key, "New value: " + value + " (v" + config.getVersion() + ")");

        return configRepository.save(config);
    }

    public record AuthResult(String accessToken, String userId, String role, String identifier) {}
}
