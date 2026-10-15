# ADR 0003: Redis Real-Time Feature Store and Sliding Window Key Design

## Status
Accepted

## Context
Requirement RSK-2 mandates real-time feature aggregation for rolling counts, sums, distinct counterparties, and ratios across 5-minute, 1-hour, 24-hour, and 7-day windows. Requirement NFR-1 sets a strict p95 decision latency under 100 ms at 1,000 events/second. Individual sequential network round-trips to Redis during scoring would quickly violate the SLA budget.

## Decision
1. **Sliding Window Key Design**:
   - Keys are prefixed by domain and entity: `feat:{entity_type}:{entity_id}:{feature_name}`.
   - For rolling sums and counts, time-bucketed Redis sorted sets (`ZSET`) store member `"{timestamp_ms}:{event_id}"` with score `timestamp_ms`.
   - Distinct counterparties use HyperLogLog (`PFADD`/`PFCOUNT`) for large windows or sorted sets with counterparty IDs for exact short-window calculations.
   - Merchant-specific features (near-limit count, round amount count, repeat payer count) use hash fields or dedicated time buckets.
2. **Pipelining and Atomic Lua Scripts**:
   - Feature queries are executed in a single pipelined Redis transaction, fetching all window horizons (5m, 1h, 24h, 7d) in 1 network round-trip (< 5 ms).
   - Pruning of expired elements (`ZREMRANGEBYSCORE`) is deferred or executed asynchronously during ingestion updates.
3. **Resilience & Fallback (NFR-5)**:
   - When Redis is unreachable or times out (> 20 ms), the risk engine does not crash or block.
   - It degrades safely by returning `ALLOW_WITH_FLAG` with decision code `DEGRADED_FEATURE_STORE_TIMEOUT`, emitting an alert to Kafka for analyst visibility.

## Consequences
- Predictable, sub-millisecond in-memory feature reads.
- Single network hop per risk decision.
- Resilient fail-open behavior protects payment flows while flagging transactions for offline review.
