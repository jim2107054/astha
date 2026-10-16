package com.astha.disputes.service;

import com.astha.disputes.domain.Dispute;
import com.astha.disputes.domain.DisputeEvent;
import com.astha.disputes.repository.DisputeEventRepository;
import com.astha.disputes.repository.DisputeRepository;
import com.astha.disputes.sla.BangladeshSlaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final DisputeEventRepository eventRepository;
    private final BangladeshSlaCalculator slaCalculator;

    // Allowed transition map for the dispute state machine
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "SUBMITTED", Set.of("UNDER_INVESTIGATION", "MERCHANT_PENDING", "RESOLVED_REFUNDED"),
            "UNDER_INVESTIGATION", Set.of("EVIDENCE_REQUESTED", "RESOLVED_REFUNDED", "RESOLVED_REJECTED", "ESCALATED"),
            "EVIDENCE_REQUESTED", Set.of("UNDER_INVESTIGATION", "RESOLVED_REJECTED"),
            "MERCHANT_PENDING", Set.of("UNDER_INVESTIGATION", "RESOLVED_REFUNDED", "RESOLVED_REJECTED"),
            "ESCALATED", Set.of("RESOLVED_REFUNDED", "RESOLVED_REJECTED")
    );

    public DisputeService(DisputeRepository disputeRepository,
                          DisputeEventRepository eventRepository,
                          BangladeshSlaCalculator slaCalculator) {
        this.disputeRepository = disputeRepository;
        this.eventRepository = eventRepository;
        this.slaCalculator = slaCalculator;
    }

    @Transactional
    public Dispute fileDispute(String txnId, String senderAccountId, String counterpartyAccountId,
                               String disputeType, Long amountPaisa, String filingSource, String note) {
        // Enforce deduplication by transaction id
        Optional<Dispute> existing = disputeRepository.findByTxnId(txnId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Calculate Bangladesh Bank regulatory SLA: default 3 working days (excluding Fri & Sat)
        int workingDays = "UNAUTHORIZED".equalsIgnoreCase(disputeType) ? 30 : 3;
        LocalDateTime deadline = slaCalculator.calculateWorkingDaysDeadline(LocalDateTime.now(), workingDays);

        String disputeId = "DSP-" + UUID.randomUUID().toString().substring(0, 8);
        Dispute dispute = new Dispute(
                disputeId, txnId, senderAccountId, counterpartyAccountId,
                disputeType, amountPaisa, "SUBMITTED", filingSource, deadline
        );
        dispute = disputeRepository.save(dispute);

        eventRepository.save(new DisputeEvent(
                UUID.randomUUID().toString(), disputeId, null, "SUBMITTED",
                filingSource, "Dispute created: " + note
        ));

        return dispute;
    }

    @Transactional
    public Dispute transitionStatus(String disputeId, String targetStatus, String reason, String actorId) {
        Dispute dispute = getDispute(disputeId);
        String currentStatus = dispute.getStatus();

        // Terminal state check
        if ("RESOLVED_REFUNDED".equals(currentStatus) || "RESOLVED_REJECTED".equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transition from terminal state: " + currentStatus);
        }

        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(targetStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Illegal state transition from %s to %s", currentStatus, targetStatus)
            );
        }

        dispute.setStatus(targetStatus);
        dispute = disputeRepository.save(dispute);

        eventRepository.save(new DisputeEvent(
                UUID.randomUUID().toString(), disputeId, currentStatus, targetStatus,
                actorId, reason
        ));

        return dispute;
    }

    @Transactional
    public Dispute resolveDispute(String disputeId, String outcome, String note, String actorId) {
        Dispute dispute = getDispute(disputeId);
        String currentStatus = dispute.getStatus();

        if ("RESOLVED_REFUNDED".equals(currentStatus) || "RESOLVED_REJECTED".equals(currentStatus)) {
            return dispute; // Idempotent resolution
        }

        String targetStatus = outcome.startsWith("REFUND") ? "RESOLVED_REFUNDED" : "RESOLVED_REJECTED";
        dispute.setStatus(targetStatus);
        dispute.setResolutionOutcome(outcome);
        dispute.setResolutionNote(note);
        dispute = disputeRepository.save(dispute);

        eventRepository.save(new DisputeEvent(
                UUID.randomUUID().toString(), disputeId, currentStatus, targetStatus,
                actorId, "Resolved with outcome: " + outcome + " - " + note
        ));

        return dispute;
    }

    @Transactional
    public Dispute autoRefundDebitedFailed(String txnId, String senderAccountId, String counterpartyAccountId,
                                           Long amountPaisa) {
        Optional<Dispute> existing = disputeRepository.findByTxnId(txnId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String disputeId = "DSP-AUTO-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime deadline = LocalDateTime.now();

        Dispute dispute = new Dispute(
                disputeId, txnId, senderAccountId, counterpartyAccountId,
                "FAILED_TXN", amountPaisa, "RESOLVED_REFUNDED", "AUTO_REFUND", deadline
        );
        dispute.setResolutionOutcome("REFUND_FULL");
        dispute.setResolutionNote("Auto-refund issued: transaction confirmed debited-but-failed at provider");
        dispute = disputeRepository.save(dispute);

        eventRepository.save(new DisputeEvent(
                UUID.randomUUID().toString(), disputeId, "SUBMITTED", "RESOLVED_REFUNDED",
                "SYSTEM", "Automated refund executed without customer friction"
        ));

        return dispute;
    }

    public List<Dispute> getSlaQueue() {
        List<Dispute> disputes = disputeRepository.findAllByOrderByDeadlineUtcAsc();
        LocalDateTime now = LocalDateTime.now();
        for (Dispute d : disputes) {
            if (d.getDeadlineUtc() != null) {
                var status = slaCalculator.evaluateStatus(d.getDeadlineUtc(), now);
                d.setWarningLevel(status.warningLevel());
            }
        }
        return disputes;
    }

    public Dispute getDispute(String disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute not found: " + disputeId));
    }

    public List<DisputeEvent> getDisputeTimeline(String disputeId) {
        getDispute(disputeId);
        return eventRepository.findByDisputeIdOrderByTimestampAsc(disputeId);
    }
}
