# Astha Platform Operations Runbooks

**Audience:** Site Reliability Engineers (SRE), Risk Operations Leads  
**Updated:** 2026-10-16  

---

## Runbook 1: Kafka Consumer Lag High Alert (`consumer_lag > 500`)

### Symptoms
- Prometheus alert `KafkaConsumerLagHigh` fires.
- Latency between transaction event timestamp and risk alert generation increases beyond 1 second.

### Diagnostic Steps
1. Identify offending consumer group:
   ```bash
   kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group risk-engine-group
   ```
2. Check `risk-service` pod CPU utilization:
   ```bash
   kubectl top pods -n astha-prod -l app=risk-service
   ```
3. Inspect Redis connection latency and cluster health:
   ```bash
   redis-cli -h redis ping
   redis-cli -h redis --latency
   ```

### Remediation
1. Scale up `risk-service` worker pods:
   ```bash
   kubectl scale deployment/risk-service -n astha-prod --replicas=8
   ```
2. If partition imbalance is detected, trigger consumer group rebalance or increase Kafka topic partitions for `transactions.v1` (default: 8 partitions).

---

## Runbook 2: Stuck Transactional Outbox Events (`outbox_lag > 100`)

### Symptoms
- Ingest service database has rows in `outbox_events` with `status = 'PENDING'` older than 2 minutes.

### Diagnostic Steps
1. Query outbox status distribution:
   ```sql
   SELECT status, count(*), min(created_at) FROM outbox_events GROUP BY status;
   ```
2. Check `ingest-service` application logs for Kafka connection timeouts or serialization errors:
   ```bash
   kubectl logs -n astha-prod -l app=ingest-service --tail=200 | grep -i ERROR
   ```

### Remediation
1. Check Kafka broker health and disk space:
   ```bash
   kubectl exec -it kafka-0 -n astha-prod -- df -h /var/lib/kafka/data
   ```
2. Restart `ingest-service` outbox poller if lock contention was detected:
   ```bash
   kubectl rollout restart deployment/ingest-service -n astha-prod
   ```

---

## Runbook 3: Bangladesh Bank Working-Day SLA Breach Risk Alert

### Symptoms
- Prometheus alert `DisputeSlaBreachWarning` fires.
- Cases in Dispute Desk indicate less than 4 hours remaining on 3-day same-provider or 30-day cross-provider clock.

### Diagnostic Steps
1. Query high-risk disputes via API:
   ```bash
   curl -H "Authorization: Bearer $STAFF_TOKEN" http://api-gateway:8090/v1/disputes/queue?sortBy=risk_score
   ```
2. Check provider mock / DMS partner API endpoint availability.

### Remediation
1. Re-trigger automated refund loop for verified failed debits:
   ```bash
   curl -X POST http://api-gateway:8090/v1/disputes/auto-refund/trigger
   ```
2. For merchant disputes awaiting counter-evidence past 48h, execute provisional customer credit or escalate to arbitration.
