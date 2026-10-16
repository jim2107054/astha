package com.astha.disputes.controller;

import com.astha.disputes.domain.Dispute;
import com.astha.disputes.domain.DisputeEvent;
import com.astha.disputes.service.DisputeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @GetMapping
    public ResponseEntity<List<Dispute>> getSlaQueue() {
        return ResponseEntity.ok(disputeService.getSlaQueue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.getDispute(id));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<DisputeEvent>> getTimeline(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.getDisputeTimeline(id));
    }

    @PostMapping
    public ResponseEntity<Dispute> fileDispute(
            @RequestBody Map<String, Object> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "customer-01") String actorId) {
        String txnId = (String) req.get("txnId");
        String senderId = (String) req.get("senderAccountId");
        String counterpartyId = (String) req.getOrDefault("counterpartyAccountId", "UNKNOWN");
        String disputeType = (String) req.getOrDefault("disputeType", "FAILED_TXN");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;
        String source = (String) req.getOrDefault("filingSource", "CUSTOMER");
        String note = (String) req.getOrDefault("note", "Customer filed dispute");

        return ResponseEntity.ok(disputeService.fileDispute(txnId, senderId, counterpartyId, disputeType, amount, source, note));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<Dispute> transitionStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "officer-01") String actorId) {
        String targetStatus = req.get("targetStatus");
        String reason = req.getOrDefault("reason", "Status transition");

        return ResponseEntity.ok(disputeService.transitionStatus(id, targetStatus, reason, actorId));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Dispute> resolveDispute(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            @RequestHeader(value = "X-Actor-Id", defaultValue = "officer-01") String actorId) {
        String outcome = req.get("outcome");
        String note = req.getOrDefault("note", "Dispute resolved");

        return ResponseEntity.ok(disputeService.resolveDispute(id, outcome, note, actorId));
    }

    @PostMapping("/auto-refund")
    public ResponseEntity<Dispute> autoRefund(
            @RequestBody Map<String, Object> req) {
        String txnId = (String) req.get("txnId");
        String senderId = (String) req.get("senderAccountId");
        String counterpartyId = (String) req.getOrDefault("counterpartyAccountId", "UNKNOWN");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;

        return ResponseEntity.ok(disputeService.autoRefundDebitedFailed(txnId, senderId, counterpartyId, amount));
    }
}
