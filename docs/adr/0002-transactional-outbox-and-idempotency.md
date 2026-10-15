# ADR 0002: Transactional Outbox and Universal Idempotency

## Status
Accepted

## Context
In financial systems and fraud operations, network partitions, retries, and duplicate requests can lead to double refunds, duplicate decisions, and inconsistent state. Non-negotiable rules 2 and 3 mandate strict idempotency on all write endpoints and a transactional outbox for all asynchronous event publishing.

## Decision
1. **Universal Idempotency**:
   - Every mutating HTTP endpoint requires an `Idempotency-Key` header.
   - When a request arrives with an `Idempotency-Key`, the system hashes the request body (SHA-256).
   - If an existing record exists with the same key and matching hash, the cached response (status, headers, body) is returned immediately with `200/201`.
   - If the same key arrives with a different body payload, an RFC 9457 Problem Details response with HTTP status `422 Unprocessable Content` is returned.
   - The idempotency record is persisted in the exact same database transaction as the entity state mutation.
2. **Transactional Outbox**:
   - Services do not publish directly to Kafka inside request-handling threads.
   - Events are written to an `outbox_events` table in the local service schema as part of the domain transaction.
   - A dedicated poller/CDC worker publishes pending events to Kafka in order by partition key (entity ID) and marks records as published.
3. **Idempotent Consumers**:
   - All Kafka consumers check a `processed_events` table before applying state updates.
   - Re-delivered events are acknowledged without repeating side effects.

## Consequences
- Guaranteed at-least-once message delivery with exactly-once consumer execution semantics.
- No lost events during service crashes between DB commit and broker publish.
- Zero duplicate decisions or duplicate refunds.
