# Astha STRIDE Threat Model & Security Posture

**Framework:** Microsoft STRIDE Threat Modeling  
**Requirements Covered:** NFR-5, NFR-6, NFR-7, NFR-8, AUD-1, AUD-2, ADM-1  
**Date:** 2026-10-16  

---

## 1. System Asset Inventory & Trust Boundaries

1. **Internet / Public Boundary:** Customer PWA (`apps/customer`), External MFS Providers (bKash, Nagad, Rocket webhooks).
2. **DMZ / Perimeter:** `api-gateway` (TLS termination, correlation ID injection, rate limiting, JWT validation).
3. **Internal Core Network:** `ingest-service`, `risk-service`, `graph-service`, `case-service`, `dispute-service`, `identity-service`, Kafka, Redis, Postgres.
4. **Analyst Trust Zone:** Analyst Console (`apps/console`) with audited role-based unmasking.

---

## 2. STRIDE Analysis & Defense Controls

| STRIDE Category | Threat Scenario | Mitigation in Astha | Verification Test |
| :--- | :--- | :--- | :--- |
| **Spoofing (S)** | Attacker attempts to forge MFS transaction webhooks or impersonate bank officer. | HMAC-SHA256 signature verification on provider callbacks; JWT tokens with rotating refresh tokens and token-reuse blacklisting in `identity-service`. | `IdentityServiceIntegrationTest` |
| **Tampering (T)** | Corrupting transaction logs or altering freeze audit history to hide mule collusion. | Hash-chained append-only `audit_log` with SHA-256 integrity proofs; strict four-eyes maker-checker state machine in Postgres. | `CaseServiceIntegrationTest`, `AUD-1` |
| **Repudiation (R)** | Analyst denies freezing an innocent account or maker denies requesting customer refund. | Immutable audit log captures `actor_id`, `client_ip`, timestamp, before-and-after state for every maker-checker action and PII unmasking event. | `AUD-1`, `AUD-2` |
| **Information Disclosure (I)** | Customer NID, phone numbers, and full balances leaked in browser DOM or logs. | Strict PII masking by default (`+88017****3344`, `NID: 1994**********01`); audited click-to-reveal API requiring supervisor permission. | `analyst-workflow.spec.ts` |
| **Denial of Service (D)** | High-frequency API spam exhausting database connections or Kafka partitions. | Sliding-window IP and API-key rate limiting (Redis token bucket); HPA pod autoscaling (3-16 pods) under CPU > 70%. | `NFR-10`, `api-gateway` |
| **Elevation of Privilege (E)** | An analyst attempting to approve their own mule freeze action to bypass four-eyes. | Server-side verification enforcing `makerId != approverId`; requests fail immediately with HTTP 403 Forbidden. | `CaseServiceIntegrationTest` |
