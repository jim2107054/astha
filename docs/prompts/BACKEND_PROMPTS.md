# Astha: Backend Team Prompts

For Claude Code or Antigravity. Read `README.md` for the workflow. Put `SRS.md` at `docs/SRS.md` in your repo.

## How to use

1. Create the repo, copy `SRS.md` into `docs/SRS.md`, and save the shared brief below as `CLAUDE.md` in the repo root (Antigravity: add it as the project rules file).
2. Run one phase prompt at a time, in order. Each phase ends with tests that must pass.
3. After each phase: read the diff, run `./gradlew check` and `docker compose up`, commit, then continue.
4. If the agent drifts, stop it and say: "Re-read CLAUDE.md and docs/SRS.md, list the requirement IDs you are implementing, then continue."

## Shared brief (save as CLAUDE.md)

```text
Project: Astha, a real-time fraud and dispute operations platform for mobile financial services in Bangladesh. All data is synthetic. No real money, no real PII.
Read docs/SRS.md first. Implement by requirement ID (ING-, RSK-, GRP-, CAS-, DSP-, SIM-, EVL-, ML-, ADM-, AUD-, NTF-, NFR-). Put IDs in commit messages and test names.

Stack: Java 21, Spring Boot 3, Gradle multi-module, PostgreSQL 16 with Flyway (one schema per service), Kafka (KRaft), Redis, Testcontainers, JUnit 5, OpenAPI 3.1, Docker Compose, Kubernetes manifests, OpenTelemetry, Prometheus, Grafana. Optional model-service in Python 3.12 with FastAPI and LightGBM.
Services: api-gateway, identity-service, ingest-service, risk-service, graph-service, case-service, dispute-service, notification-service, provider-mock, simulator-service, analytics (read API), optional model-service.

Non-negotiable rules:
1. Amounts are BIGINT paisa. Times are UTC in storage, Asia/Dhaka in display.
2. Every write endpoint takes an Idempotency-Key. Same key and same body returns the stored response; same key and different body returns 422. Store the key in the same DB transaction as the change.
3. Events leave a service only through a transactional outbox. Consumers are idempotent with a processed_events table. Event schemas live in contracts/ and are validated in CI.
4. Every risk decision stores reason codes, rule versions and a replayable feature snapshot. No score without an explanation.
5. Irreversible actions (freeze, rejection) are recommendations that need human action; ring-level and high-value freezes need a different approver (four-eyes), enforced on the server.
6. Rules, thresholds, SLA durations and the holiday calendar are data in the database, versioned, audited and changeable without redeploy. Never hard-code the regulatory numbers; they are defaults from the reported Bangladesh Bank guideline and must be configurable.
7. Bangladesh working week is Friday and Saturday off. Use a calendar table for working-day math and test it.
8. audit_log, dispute_events and evidence are append-only; enforce with database triggers and test them.
9. Phone numbers and NIDs are field-encrypted at rest and masked in logs and UI.
10. Errors use RFC 9457 problem details. Never log PII, OTPs or tokens.
11. Every change ships with tests. Concurrency and retry paths get dedicated tests.
12. Explain non-obvious choices in docs/adr/NNNN-title.md. Keep modules small.
13. Before finishing a task: run ./gradlew check, run the docker compose smoke test, update OpenAPI and README.
If a requirement is ambiguous, pick the safest interpretation and record it in an ADR.
```

---

## Phase B0: Foundation

Requirements: NFR-10, NFR-11, NFR-14.

```text
Read CLAUDE.md and docs/SRS.md. Create the monorepo.

1. Gradle multi-module with empty Spring Boot apps for every service in CLAUDE.md, plus a shared platform-lib module containing: problem-details handler, idempotency filter and store interface, transactional outbox starter with a poller, processed-events helper, tracing and logging setup (trace ids, PII masking), security helpers (JWT validation, RBAC annotations), and a Clock abstraction for tests.
2. docker-compose.yml: one Postgres per service (or one server with separate schemas), Kafka KRaft, Redis, MinIO (evidence files), Prometheus, Grafana, Jaeger. Healthchecks and profiles (core, full).
3. Flyway baseline migration per service. Testcontainers base test classes.
4. contracts/ folder with JSON Schema for events and a CI step that validates producers and consumers against it.
5. GitHub Actions: build, test, dependency scan, secret scan, container build, compose smoke test.
6. Makefile with up, down, test, seed, load-test. README quickstart.
7. ADR-0001 (service boundaries), ADR-0002 (outbox and idempotent consumers), ADR-0003 (configurable regulatory clocks).

Done when docker compose up starts everything, every service exposes /actuator/health through the gateway, and CI is green.
```

## Phase B1: Simulator and provider-mock

Requirements: SIM-1 to SIM-6, provider side of DSP-3.

```text
Implement provider-mock and simulator-service.

provider-mock: accounts with balances and statuses, transactions with statuses (SUCCESS, FAILED, PENDING, REVERSED), a status lookup endpoint, idempotent refund endpoint, idempotent freeze and unfreeze endpoints, cash-out and merchant QR operations, configurable latency and failure injection (timeouts, duplicate callbacks, debited-but-failed cases).

simulator-service:
- Seeded generator for a synthetic population: customers, agents, merchants with Bangladesh-style names, phone formats, districts, KYC tiers. Use a documented seed; the same seed must give identical output (test it).
- Normal behavior models by segment: salary days, family P2P, bill payments, merchant QR, cash-in and cash-out with weekly and festival seasonality, plus hard negatives (legitimate high-volume merchants, busy agents).
- Fraud typology injectors matching SRS section 8: pass-through mule chains, young-account bursts, QR cash-substitute merchants, account takeover after SIM or device change, structuring just under a limit, agent anomalies, and multi-account rings sharing devices.
- Failed-transaction and dispute-worthy scenarios.
- Control API: start, stop, set rate, add scenario, reset with seed. Emits events to ingest-service via HTTP or directly to Kafka in bulk mode.
- Writes a ground_truth table: entity or event id, typology, ring id, label, injected time.

Tests: determinism by seed, typology injectors produce the intended patterns, provider-mock idempotency and failure injection. Add docs/simulator.md describing every typology and its parameters.
```

## Phase B2: Ingestion and contracts

Requirements: ING-1 to ING-6, NFR-3, NFR-4.

```text
Implement ingest-service.

- POST /v1/events/transactions, /batch, /profile with API-key auth for providers. Validate schema, amounts, timestamps and skew. Deduplicate by event_id with a unique constraint and return the original result on duplicates.
- Status updates link to the original event and are applied in order or buffered when out of order.
- Normalize events and publish txn.events.v1 and profile.events.v1 through the outbox, keyed by account id for ordering.
- Dead-letter topic with reason codes and an admin view endpoint. Replay endpoint to re-publish a time window.
- Metrics: accepted, duplicate, invalid, dead-lettered, lag.

Tests: duplicate storms, out-of-order status, schema violations, crash between DB commit and Kafka publish (outbox recovers), 3,000 events per second batch test. Update contracts/ and OpenAPI.
```

## Phase B3: Risk engine

Requirements: RSK-1 to RSK-8, rules R01 to R10, NFR-1, NFR-5, NFR-6.

```text
Implement risk-service.

1. Consume txn.events.v1 and profile.events.v1 (idempotently). Maintain real-time features in Redis: rolling counts, sums, distinct counterparties and ratios over 5 minutes, 1 hour, 24 hours and 7 days; account age; device and beneficiary novelty; per-merchant round-amount ratio, payer repeat rate, near-limit ratio, settlement ratio; per-agent velocity baselines. Use atomic Lua scripts or sorted sets and keep reads to one pipelined round trip.
2. Rule engine: rules are data (JSON logic or a small expression language) with parameters, weights, severity and typology, loaded from the database and cached with versioned invalidation. Implement R01 to R10 from SRS section 8.
3. Scoring: combine rule hits into a 0 to 100 score and a decision (ALLOW, REVIEW, BLOCK_RECOMMENDED). Store decisions with reason codes, rule versions and the feature snapshot. Reason codes have Bengali and English texts.
4. Synchronous POST /v1/risk/score with a 100 ms budget and a safe fallback (ALLOW_WITH_FLAG plus alert) when Redis or the model is down.
5. Rule lifecycle: versions, draft, approve by a different user, activate without redeploy, rollback, audit entries. Shadow mode for a candidate version storing results in shadow_results. Back-test endpoint against stored history with labels.
6. Allowlist and blocklist with expiry.
7. Publish risk.decisions.v1 and alerts.v1 through the outbox.

Tests: each rule with boundary values, a golden-file test that replays a feature snapshot to the same score, fallback behavior with Redis down, approval enforcement (same user cannot approve own change), and a latency test under 1,000 events per second. Document the Redis key design in an ADR.
```

## Phase B4: Graph service

Requirements: GRP-1 to GRP-5.

```text
Implement graph-service.

- Consume txn.events.v1 and profile.events.v1 and maintain an entity graph in Postgres (entities: account, device, phone, agent, merchant; edges: transfers with time and amount, shared device, shared phone, agent-customer). Index for neighbor queries up to depth 3.
- Scheduled and on-demand detection jobs: connected components over a sliding window, fan-in then fast fan-out patterns, shared-device clusters, young-account density, escalation from small to large amounts. Produce ring candidates with a score and an evidence path (the edges that justify the score).
- Publish ring.detected alerts through the outbox. Provide GET /v1/graph/entities/{id}/neighbors and /v1/rings endpoints.
- Agent-level anomaly detection (z-score of cash-out velocity against peers, customers per device, repeated round amounts).
- Make job runs idempotent and resumable; record graph_jobs.

Tests: seeded rings from the simulator are found; legitimate busy merchants are not flagged; evidence paths are valid; job re-runs do not duplicate rings. Compare job time at 100,000 customers and record in docs/performance.md.
```

## Phase B5: Cases, triage and feedback

Requirements: CAS-1 to CAS-10.

```text
Implement case-service.

1. Consume alerts.v1 and ring.detected. Deduplicate and group alerts into cases by entity (account, device, merchant, ring) within a configurable window; merging cases must be idempotent and auditable.
2. Triage priority: combine max risk score, amount at risk, entity history, typology severity and alert count; store the component breakdown so the UI can show it.
3. Analyst APIs: assign, comment, label alerts (TRUE_FRAUD, FALSE_POSITIVE, UNCLEAR), recommend freeze, request approval, close with reason.
4. Four-eyes approvals enforced on the server: approver differs from requester; amount and ring-level rules from configuration. On approval, call provider-mock to freeze or hold idempotently and store the outcome.
5. Feedback: publish feedback.v1; compute per-rule, per-typology and per-segment precision in rule_quality; flag rules below a configurable floor; produce threshold suggestions from labelled history for the risk manager (never auto-applied).
6. Time-to-first-touch SLA with warnings.
7. Export a case report draft as JSON and PDF (timeline, entities, evidence, reason codes).

Tests: grouping and merging, triage ordering, four-eyes bypass attempts, idempotent action execution under retry, precision calculation against known labels, report content. Add an ADR on grouping keys and windows.
```

## Phase B6: Disputes and SLA

Requirements: DSP-1 to DSP-12, NTF-1, NTF-2, ADR-0003.

```text
Implement dispute-service and notification-service.

dispute-service:
- Dispute types: FAILED_TXN, WRONG_TRANSFER, MERCHANT_DISPUTE, UNAUTHORIZED, QR_TAMPERING. Create from customer, officer or provider event. Enforce the filing window from configuration (default 45 days); late filings route to manual review with permission.
- Auto-refund: a worker finds debited-but-failed transactions, confirms with provider-mock status lookup, issues an idempotent refund and creates a resolved dispute record without customer action; notify the customer.
- State machine from SRS section 9 with every transition stored in append-only dispute_events and validated (illegal transitions rejected).
- SLA clocks from configuration: same-provider resolution (default 3 working days), overall maximum (default 30 days), fraud investigation (default 30 days), merchant response deadline. Working-day math uses a calendar table with Friday and Saturday as weekends plus holidays. Warnings at 50, 80 and 100 percent; breach flags; a breach prediction score based on remaining time and queue state.
- Cross-provider disputes handled by a simulated NPSB DMS adapter with status sync and idempotent callbacks.
- Evidence: upload to MinIO with type and size limits, checksum, append-only metadata, immutable after submission, virus-scan placeholder.
- Merchant response workflow and deadline.
- Resolutions: REFUND_FULL, REFUND_PARTIAL, REJECTED, CHARGEBACK, ESCALATED_TO_ARBITRATION with idempotent refund requests.
- Link disputes and cases; a confirmed UNAUTHORIZED dispute creates or upgrades a fraud case and emits labels to feedback.v1.
- Officer queue API sorted by deadline risk; bulk actions and templates.

notification-service: consume disputes.v1 and notifications.v1, render Bengali and English templates, send to a mock SMS and email sink with retries and idempotency, expose a dev inbox.

Tests: working-day edge cases (Thursday evening, weekends, holidays, month ends), each state transition and each illegal one, auto-refund race with a manual refund, evidence immutability, clock warnings, cross-provider callbacks out of order. Property test: no dispute can be refunded twice.
```

## Phase B7: Identity, audit, admin, analytics

Requirements: ADM-1 to ADM-4, AUD-1, AUD-2, EVL-4, EVL-5.

```text
Implement identity-service, audit, admin APIs and the analytics read API.

identity-service: phone OTP for customers (mock sink), email plus password plus TOTP for staff, JWT access tokens (15 minutes) and rotating refresh tokens with reuse detection, RBAC roles (ANALYST, APPROVER, DISPUTE_OFFICER, RISK_MANAGER, COMPLIANCE, ADMIN, MERCHANT, CUSTOMER), lockout, rate limiting with Redis, provider API keys (hashed, rotate, revoke), JWKS endpoint.

Audit: central append-only audit_log fed by audit.v1 with actor, IP, time, before and after, searchable by admin and compliance. Masking policy: PII masked by default, reveal requires permission and writes an audit entry.

Admin APIs: thresholds, SLA durations, holiday calendar, templates, feature flags, all versioned with change preview and audit.

analytics: materialized views or incremental aggregates for KPIs (alerts, cases, false-positive rate by rule, prevented loss in the simulator, SLA compliance, mean time to resolve, auto-refund share) and drill-down endpoints back to cases and disputes.

Tests: complete RBAC matrix for every endpoint, token reuse detection, audit immutability, masking, KPI numbers against a known seeded dataset.
```

## Phase B8: Evaluation harness and model

Requirements: EVL-1 to EVL-3, ML-1, ML-2, NFR-15.

```text
Build the evaluation harness and optional model-service.

1. Evaluation harness (a Gradle module or Python tool): load a seeded dataset and ground truth, replay through the real pipeline at accelerated time, then compute per typology precision, recall, F1, false-positive rate, detection delay, alerts per true fraud, case counts, and analyst-minutes saved using a documented handling-time model. Compare four configurations: A rules only, B plus graph, C plus alert grouping, D plus shadow model. Run across at least three seeds and report mean and spread.
2. Generate docs/evaluation.md with method, seeds, tables, charts and an honest limitations section. The same seed must reproduce the same numbers (test it).
3. Dispute simulation: 5,000 disputes, report SLA compliance, auto-refund share, breach prediction accuracy and mean time to resolve.
4. Optional model-service: Python FastAPI service with a LightGBM model trained on simulator data with time-based splits (no leakage), a model card, feature parity tests against risk-service, and a scoring endpoint. Run in shadow mode only; promotion requires an approved rule-version change.

Be honest: report the numbers you get. Add hard negatives and noise so results are not trivially perfect.
```

## Phase B9: Production readiness

Requirements: NFR-1 to NFR-10.

```text
Harden the system.

- OpenTelemetry tracing across services with trace ids in logs; Prometheus metrics (decision_latency, redis_feature_latency, consumer_lag, outbox_lag, alerts_total, cases_open, sla_breaches, refunds_total, dead_letters_total); Grafana dashboards as JSON; alert rules.
- Load tests with k6: ingest at 3,000 events per second for 10 minutes, risk score at 1,000 per second, mixed API traffic. Profile the top bottleneck (Redis pipeline, Kafka partitions, DB pool, GC), fix it, and record before and after numbers in docs/performance.md.
- Chaos tests: kill risk-service, Redis, Kafka broker and provider-mock during load; show no accepted event lost, no duplicate refund, safe fallback behavior, and recovery time.
- Security: OWASP ZAP baseline and Trivy in CI, dependency and secret scans, threat model (STRIDE) in docs/threat-model.md, rate limiting and lockout tests, RBAC matrix test.
- Kubernetes manifests (Deployments, Services, HPA, ConfigMaps, PodDisruptionBudgets) and a Helm chart or Kustomize overlays.
- Docs: architecture overview with sequence diagrams (score and alert, mule freeze with four-eyes, dispute with auto-refund), runbooks (consumer lag, stuck outbox, DMS callback failures), data retention note, traceability matrix mapping every Must requirement to tests (docs/traceability.md), and a 3-minute demo script (docs/demo.md) with seed data.
- Final check: run the full invariants (no duplicate decisions, no duplicate refunds, audit log complete) after the load and chaos runs and include results in the docs.
```

## Backend definition of done

- All Must requirements in SRS section 3 have tests and appear in `docs/traceability.md`.
- Acceptance criteria 1 to 10 in SRS section 11 pass on a clean machine.
- Load, chaos and evaluation reports are committed and reproducible.
- No secret or PII in the repo or logs.
