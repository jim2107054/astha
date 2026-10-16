package com.astha.providermock.controller;

import com.astha.providermock.domain.MockAccount;
import com.astha.providermock.domain.MockTransaction;
import com.astha.providermock.service.ProviderMockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/provider")
public class ProviderMockController {

    private final ProviderMockService providerService;

    public ProviderMockController(ProviderMockService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<MockAccount> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(providerService.getAccount(id));
    }

    @PostMapping("/accounts")
    public ResponseEntity<MockAccount> createAccount(@RequestBody Map<String, Object> req) {
        String accountId = (String) req.get("accountId");
        String msisdn = (String) req.get("msisdn");
        String name = (String) req.get("accountHolderName");
        Long balance = req.get("balancePaisa") instanceof Number n ? n.longValue() : 0L;
        String kycTier = (String) req.getOrDefault("kycTier", "TIER_1");
        String type = (String) req.getOrDefault("accountType", "CUSTOMER");
        String deviceId = (String) req.getOrDefault("deviceFingerprint", "dev-default");

        return ResponseEntity.ok(providerService.createAccount(accountId, msisdn, name, balance, kycTier, type, deviceId));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<MockTransaction> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(providerService.getTransaction(id));
    }

    @PostMapping("/transactions")
    public ResponseEntity<MockTransaction> executeTransaction(
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String senderId = (String) req.get("senderAccountId");
        String receiverId = (String) req.get("receiverAccountId");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;
        String type = (String) req.getOrDefault("txnType", "P2P");

        return ResponseEntity.ok(providerService.executeTransaction(senderId, receiverId, amount, type, idempotencyKey));
    }

    @PostMapping("/transactions/refund")
    public ResponseEntity<ProviderMockService.RefundResult> refund(
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String txnId = (String) req.get("txnId");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;
        String reason = (String) req.getOrDefault("reason", "CUSTOMER_DISPUTE");

        return ResponseEntity.ok(providerService.refundTransaction(txnId, amount, reason, idempotencyKey));
    }

    @PostMapping("/accounts/{id}/freeze")
    public ResponseEntity<ProviderMockService.AccountStatusResult> freezeAccount(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String reason = req != null && req.containsKey("reason") ? (String) req.get("reason") : "FRAUD_INVESTIGATION";
        return ResponseEntity.ok(providerService.freezeAccount(id, reason, idempotencyKey));
    }

    @PostMapping("/accounts/{id}/unfreeze")
    public ResponseEntity<ProviderMockService.AccountStatusResult> unfreezeAccount(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String reason = req != null && req.containsKey("reason") ? (String) req.get("reason") : "DISPUTE_CLEARED";
        return ResponseEntity.ok(providerService.unfreezeAccount(id, reason, idempotencyKey));
    }

    @PostMapping("/config/simulate")
    public ResponseEntity<Map<String, String>> configureSim(
            @RequestBody Map<String, Object> req) {
        long latency = req.containsKey("latencyMs") ? ((Number) req.get("latencyMs")).longValue() : 0L;
        boolean timeout = req.containsKey("timeout") && Boolean.TRUE.equals(req.get("timeout"));
        double failureRate = req.containsKey("failureRate") ? ((Number) req.get("failureRate")).doubleValue() : 0.0;

        providerService.configureSimulation(latency, timeout, failureRate);
        return ResponseEntity.ok(Map.of("status", "CONFIGURED"));
    }
}
