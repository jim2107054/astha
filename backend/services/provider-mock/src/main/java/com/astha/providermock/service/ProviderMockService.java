package com.astha.providermock.service;

import com.astha.providermock.domain.MockAccount;
import com.astha.providermock.domain.MockTransaction;
import com.astha.providermock.repository.MockAccountRepository;
import com.astha.providermock.repository.MockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProviderMockService {

    private static final Logger log = LoggerFactory.getLogger(ProviderMockService.class);

    private final MockAccountRepository accountRepository;
    private final MockTransactionRepository transactionRepository;

    // Simulation configuration
    private volatile long simulatedLatencyMs = 0;
    private volatile boolean simulateTimeout = false;
    private volatile double failureRate = 0.0;

    // Idempotency cache for refunds/freezes: key -> result payload
    private final Map<String, Object> idempotencyCache = new ConcurrentHashMap<>();

    public ProviderMockService(MockAccountRepository accountRepository,
                               MockTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void configureSimulation(long latencyMs, boolean timeout, double failureRate) {
        this.simulatedLatencyMs = latencyMs;
        this.simulateTimeout = timeout;
        this.failureRate = failureRate;
    }

    private void applySimulationEffects() {
        if (simulateTimeout) {
            throw new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Simulated MFS provider network timeout"
            );
        }
        if (simulatedLatencyMs > 0) {
            try {
                Thread.sleep(simulatedLatencyMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public MockAccount getAccount(String accountId) {
        applySimulationEffects();
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No mock account with id: " + accountId
                ));
    }

    public MockTransaction getTransaction(String txnId) {
        applySimulationEffects();
        return transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No mock transaction with id: " + txnId
                ));
    }

    @Transactional
    public MockAccount createAccount(String accountId, String msisdn, String name, Long initialBalancePaisa,
                                     String kycTier, String type, String deviceId) {
        MockAccount account = new MockAccount(accountId, msisdn, name, initialBalancePaisa, "ACTIVE", kycTier, type, deviceId);
        return accountRepository.save(account);
    }

    @Transactional
    public MockTransaction executeTransaction(String senderId, String receiverId, Long amountPaisa,
                                              String type, String idempotencyKey) {
        applySimulationEffects();

        if (idempotencyKey != null) {
            var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        MockAccount sender = accountRepository.findById(senderId).orElse(null);
        MockAccount receiver = accountRepository.findById(receiverId).orElse(null);

        if (sender != null && "FROZEN".equals(sender.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Sender account is frozen");
        }

        if (sender != null && sender.getBalancePaisa() < amountPaisa) {
            MockTransaction failed = new MockTransaction(
                    "TXN-" + UUID.randomUUID(), senderId, receiverId, amountPaisa, "BDT", type,
                    "FAILED", "INSUFFICIENT_BALANCE", idempotencyKey, Instant.now()
            );
            return transactionRepository.save(failed);
        }

        if (sender != null) {
            sender.setBalancePaisa(sender.getBalancePaisa() - amountPaisa);
            sender.setUpdatedAt(Instant.now());
            accountRepository.save(sender);
        }

        if (receiver != null) {
            receiver.setBalancePaisa(receiver.getBalancePaisa() + amountPaisa);
            receiver.setUpdatedAt(Instant.now());
            accountRepository.save(receiver);
        }

        MockTransaction txn = new MockTransaction(
                "TXN-" + UUID.randomUUID(), senderId, receiverId, amountPaisa, "BDT", type,
                "SUCCESS", null, idempotencyKey, Instant.now()
        );
        return transactionRepository.save(txn);
    }

    @Transactional
    public RefundResult refundTransaction(String txnId, Long refundAmountPaisa, String reason, String idempotencyKey) {
        applySimulationEffects();

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            return (RefundResult) idempotencyCache.get(idempotencyKey);
        }

        MockTransaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Txn ID: " + txnId));

        if ("REVERSED".equals(txn.getStatus())) {
            RefundResult already = new RefundResult(txnId, "ALREADY_REFUNDED", txn.getAmountPaisa(), Instant.now());
            if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, already);
            return already;
        }

        // Return money to sender
        if (txn.getSenderAccountId() != null) {
            accountRepository.findById(txn.getSenderAccountId()).ifPresent(sender -> {
                sender.setBalancePaisa(sender.getBalancePaisa() + refundAmountPaisa);
                sender.setUpdatedAt(Instant.now());
                accountRepository.save(sender);
            });
        }

        // Deduct from receiver if possible
        if (txn.getReceiverAccountId() != null) {
            accountRepository.findById(txn.getReceiverAccountId()).ifPresent(receiver -> {
                receiver.setBalancePaisa(Math.max(0, receiver.getBalancePaisa() - refundAmountPaisa));
                receiver.setUpdatedAt(Instant.now());
                accountRepository.save(receiver);
            });
        }

        txn.setStatus("REVERSED");
        txn.setFailureReason("REFUNDED: " + reason);
        transactionRepository.save(txn);

        RefundResult result = new RefundResult(txnId, "REFUNDED", refundAmountPaisa, Instant.now());
        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, result);
        }
        return result;
    }

    @Transactional
    public AccountStatusResult freezeAccount(String accountId, String reason, String idempotencyKey) {
        applySimulationEffects();

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            return (AccountStatusResult) idempotencyCache.get(idempotencyKey);
        }

        MockAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account ID: " + accountId));

        account.setStatus("FROZEN");
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        AccountStatusResult result = new AccountStatusResult(accountId, "FROZEN", reason, Instant.now());
        if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, result);
        return result;
    }

    @Transactional
    public AccountStatusResult unfreezeAccount(String accountId, String reason, String idempotencyKey) {
        applySimulationEffects();

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            return (AccountStatusResult) idempotencyCache.get(idempotencyKey);
        }

        MockAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account ID: " + accountId));

        account.setStatus("ACTIVE");
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        AccountStatusResult result = new AccountStatusResult(accountId, "ACTIVE", reason, Instant.now());
        if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, result);
        return result;
    }

    public record RefundResult(String txnId, String status, Long amountPaisa, Instant timestamp) {}
    public record AccountStatusResult(String accountId, String status, String reason, Instant timestamp) {}
}
