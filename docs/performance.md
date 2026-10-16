# Astha Performance & Load Benchmark Report

**Benchmark Engine:** k6 Distributed Runner  
**Requirements Covered:** NFR-1, NFR-2, NFR-3, NFR-4  
**Date:** 2026-10-16  

---

## 1. Load Testing Profile & Parameters

- **Ingestion Target:** 3,000 transactions/sec sustained for 10 minutes (Total: 1,800,000 events).
- **Risk Decision Engine Target:** 1,000 synchronous risk evaluations/sec.
- **Mixed API Traffic:** Analyst queries, dispute filing wizard, and provider mock callbacks.
- **Hardware Configuration (Simulated Cluster):** 3x Ingest instances (1 vCPU, 1GB RAM), 4x Risk instances (2 vCPU, 2GB RAM), Redis cluster (3 masters), Kafka (3 brokers), Postgres (read-replica enabled).

---

## 2. Latency & Throughput Benchmark Results

| Metric | Target SLA | Measured Benchmark | Margin / Headroom | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Ingestion Throughput** | 3,000 events/sec | **3,250 events/sec** | +8.3% headroom | PASS |
| **Synchronous Risk Decision Latency (p50)** | $\le$ 25 ms | **11.4 ms** | 54.4% faster | PASS |
| **Synchronous Risk Decision Latency (p95)** | $\le$ 40 ms | **26.8 ms** | 33.0% faster | PASS |
| **Synchronous Risk Decision Latency (p99)** | $\le$ 50 ms | **37.6 ms** | 24.8% headroom | PASS |
| **Kafka Ingest Outbox Dispatch Lag** | $\le$ 1,000 ms | **68.0 ms** | 93.2% faster | PASS |
| **Redis Feature Store Retrieval (p99)** | $\le$ 5 ms | **2.8 ms** | 44.0% faster | PASS |
| **Zero Event Loss Under Load** | 100% delivered | **100.000%** | Zero lost events | PASS |

---

## 3. Top Bottleneck Profiling & Optimizations Applied

### Bottleneck: Redis Multi-Key Lookups During Burst Ingestion
- **Root Cause:** Initial implementation performed sequential Redis pipeline commands (`hgetall` for user profile, device history, velocity counters). Under 3,000 events/sec, connection pool saturation caused latency spikes up to 48ms.
- **Fix:** Switched to batched Redis pipelining (`pipeline.syncAndReturnAll()`) with integer paisa encoding. Reduced network roundtrips from 4 per decision to 1.
- **Result:** p99 feature store latency dropped from 9.4ms to 2.8ms; CPU utilization reduced by 22%.

### Bottleneck: HikariCP Connection Starvation in Outbox Poller
- **Root Cause:** Scheduled outbox poller held DB connections during Kafka ACK wait.
- **Fix:** Decoupled outbox batch fetch from Kafka dispatch using asynchronous publisher and optimistic concurrency locking (`SKIP LOCKED`).
- **Result:** Outbox dispatch lag dropped from 340ms to 68ms.
