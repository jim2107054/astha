# Astha: Software Requirements Specification

Version 1.0 · 15 Oct 2026 · Status: ready for build

Companion files: `RESEARCH.md` (why this problem), `BACKEND_PROMPTS.md`, `FRONTEND_PROMPTS.md`.

---

## 1. Introduction

### 1.1 Purpose

This document specifies Astha, a real-time fraud and dispute operations platform for mobile financial service (MFS) and payment providers. It is the single source of truth for the backend and frontend teams (human or AI agents). Requirements have IDs; commits, tests and docs must cite them.

### 1.2 Problem statement

Providers lose money and customer trust because (a) fraud such as mule accounts, OTP-driven account takeover and Bangla QR cash-out abuse is found late and buried in noisy alerts, and (b) customer disputes are resolved slowly with no visible deadline. See `RESEARCH.md` section 2 and 3.

### 1.3 Product goals

| ID | Goal | Measured by |
| --- | --- | --- |
| G1 | Decide on each transaction event in real time with explainable reasons | p95 decision latency under 100 ms at 1,000 events per second (sandbox) |
| G2 | Detect mule rings and QR cash-out abuse that single-transaction rules miss | Recall on simulator ground truth, per typology |
| G3 | Cut analyst noise | Alert-to-case reduction and per-rule false-positive rate tracked and shown |
| G4 | Resolve disputes inside regulatory-style deadlines | SLA compliance rate, mean time to resolve |
| G5 | Keep customers informed | Complaint tracker with status and next deadline in Bengali and English |
| G6 | Be provably correct and auditable | Idempotent ingestion, immutable audit log, reproducible evaluation |

### 1.4 Scope

In scope: transaction ingestion, risk scoring, graph analysis, alert and case management, analyst feedback loop, dispute management with SLA clocks, customer complaint portal, notifications, synthetic data simulator, mock provider (freeze, refund, status), evaluation harness, dashboards, admin and audit.

Out of scope: real money movement, real customer or NID data, real provider integrations, native mobile apps, regulatory filing to authorities (a draft report export is in scope), legal advice.

### 1.5 Assumptions and constraints

- All data is synthetic. No real PII. Demo names, numbers and NIDs are generated.
- The dispute clocks follow the Bangladesh Bank Bangla QR dispute guideline as reported by [Dhaka Tribune](https://www.dhakatribune.com/business/banks/420879/bb-issues-guidelines-for-dispute-resolution-in) (effective 1 Dec 2026). All durations are configuration, not constants, because the circular text was not reviewed.
- Bangladesh working week: Friday and Saturday are weekends; the holiday list is configurable.
- Reported Bangla QR figures (cash-out charge about Tk 13 to Tk 18.50 per Tk 1,000, daily cash-out limit Tk 30,000) are defaults in the simulator and rule configuration, not hard-coded ([TBS](https://www.tbsnews.net/bangladesh/how-abuse-bangla-qr-threatens-countrys-mfs-ecosystem-1538591)).
- Astha is a portfolio system, not a licensed product, and is not affiliated with any provider or regulator.

### 1.6 Glossary

| Term | Meaning |
| --- | --- |
| MFS | Mobile financial service (wallet-based mobile money) |
| Mule account | Account used to receive and pass on stolen or illicit funds |
| Cash-out | Withdrawing wallet money as cash at an agent |
| Bangla QR | National interoperable merchant QR standard; here simulated as "merchant QR payment" |
| NPSB DMS | Central dispute management system for cross-provider disputes; simulated |
| Alert | A rule or model hit on an event or entity |
| Case | A group of related alerts for one entity or ring, worked by an analyst |
| Reason code | Machine-readable explanation for a risk score |
| SLA clock | Timer with a deadline and warning thresholds for a dispute |

---

## 2. Overall description

### 2.1 Users

| Role | Description | Main needs |
| --- | --- | --- |
| Fraud analyst | Works alerts and cases | Prioritized queue, reasons, entity graph, one-click actions |
| Senior analyst or approver | Approves account freezes and closes high-value cases | Four-eyes approval, audit trail |
| Dispute officer | Handles customer and merchant disputes | SLA-ordered queue, evidence, refund actions |
| Risk manager | Tunes rules and watches quality | Rule precision, alert volume, change history |
| Operations or compliance lead | Reports to management and authority | Dashboards, exportable reports |
| Customer | Files and tracks a complaint | Simple Bengali portal, status, deadline |
| Merchant | Responds to disputes | Evidence upload, deadline |
| Admin | Manages users, roles, configuration | Safe configuration, audit |
| Simulator operator (demo mode) | Runs scenarios | Start, stop, inject fraud typologies |

### 2.2 System context

```
Provider systems (simulated)            Astha                          People
 transaction events --------> Ingest -> Risk engine -> Alerts -> Cases ---> Analysts
 complaint channel  --------> Dispute service --------------------------> Dispute officers
 refund / freeze / status <-- Mock provider <-- actions                     Customers (portal)
                              Graph service, Model service (shadow)
                              Notifications, Audit, Dashboards
```

### 2.3 Architecture

Java 21 and Spring Boot 3 services with one Postgres schema each, Kafka for events, Redis for features and counters, an optional Python model service, and two Next.js apps.

| Service | Responsibility |
| --- | --- |
| api-gateway | JWT validation, rate limiting, routing |
| identity-service | Staff and customer auth, RBAC, TOTP for staff, API keys for provider integration |
| ingest-service | Validate and deduplicate events, normalize, publish `txn.events.v1` |
| risk-service | Real-time features, rules, scoring, decisions, reason codes, rule config |
| graph-service | Entity graph, ring and mule detection jobs |
| case-service | Alert dedupe and grouping, cases, triage score, analyst actions, feedback |
| dispute-service | Dispute lifecycle, SLA clocks, evidence, refund orchestration |
| notification-service | SMS and email mock, templates in Bengali and English |
| provider-mock | Simulated provider: accounts, balances, transaction status, refund, freeze, cash-out |
| simulator-service | Synthetic population, behavior, fraud typologies, ground-truth labels |
| model-service (optional) | LightGBM scoring in shadow mode via FastAPI |
| analytics | Materialized views and a read API for dashboards |

Event topics (all versioned, keyed by entity id): `txn.events.v1`, `risk.decisions.v1`, `alerts.v1`, `cases.v1`, `disputes.v1`, `provider.actions.v1`, `notifications.v1`, `feedback.v1`, `audit.v1`.

### 2.4 Design principles

1. Idempotent everywhere: every write has an idempotency key; consumers use a processed-events table.
2. Events leave services only through a transactional outbox.
3. Decisions are explainable: no score without reason codes and the feature values used.
4. Humans stay in control of irreversible actions: freezes and rejections need analyst or approver action; the system recommends.
5. Configuration over code for rules, thresholds, SLA durations and holidays, with versioning and audit.
6. Reproducible evaluation: a fixed seed regenerates the same dataset and metrics.

---

## 3. Functional requirements

Priority: M = must for v1.0, S = should, C = could.

### 3.1 Ingestion (ING)

| ID | Requirement | Pri |
| --- | --- | --- |
| ING-1 | Accept transaction events by REST (`POST /v1/events/transactions`) and batch; each event has event_id, type (P2P, CASH_IN, CASH_OUT, MERCHANT_QR, BILL, TOPUP, REFUND), amount in paisa, timestamps, account ids, counterparty, channel, device id, IP, agent or merchant id, status | M |
| ING-2 | Reject duplicates by event_id and return the original result | M |
| ING-3 | Validate schema, amounts, time skew; invalid events go to a dead-letter topic with reason | M |
| ING-4 | Accept status updates (SUCCESS, FAILED, PENDING, REVERSED) and link them to the original event | M |
| ING-5 | Accept profile and device events (SIM change, new device, password or PIN reset, KYC tier change) | M |
| ING-6 | Replay endpoint to re-publish events from a time window for testing | S |

### 3.2 Risk scoring (RSK)

| ID | Requirement | Pri |
| --- | --- | --- |
| RSK-1 | Compute a risk score 0 to 100 and a decision (ALLOW, REVIEW, BLOCK_RECOMMENDED) for each event, with reason codes, feature values and rule versions | M |
| RSK-2 | Maintain real-time features in Redis (rolling counts, sums, distinct counterparties, ratios over 5 minutes, 1 hour, 24 hours, 7 days) | M |
| RSK-3 | Implement the rule catalog in section 8 as data-driven rules with parameters stored in the database | M |
| RSK-4 | Rule changes are versioned, require approval by a risk manager, take effect without redeploy, and can be rolled back | M |
| RSK-5 | Shadow mode: run a candidate rule or model version on live events without affecting decisions, and store results for comparison | M |
| RSK-6 | Decision API (`POST /v1/risk/score`) for synchronous use with a 100 ms budget; degrade safely (return ALLOW_WITH_FLAG and raise an alert) if features are unavailable | M |
| RSK-7 | Back-test a rule version against stored history and show alert count, precision and recall on labelled data | S |
| RSK-8 | Allowlist and blocklist management with expiry and audit | M |

### 3.3 Graph and ring detection (GRP)

| ID | Requirement | Pri |
| --- | --- | --- |
| GRP-1 | Build an entity graph (accounts, devices, agents, merchants, phone numbers) linked by transactions and shared attributes | M |
| GRP-2 | Detect candidate mule rings: connected groups with fan-in then fast fan-out, shared devices, young accounts, and low-value-to-high-value escalation | M |
| GRP-3 | Score each ring and publish `ring.detected` alerts with member list and evidence path | M |
| GRP-4 | Provide a graph query API for the console (neighbors up to depth 3, filters by time and amount) | M |
| GRP-5 | Detect agent-level anomalies: cash-out velocity outliers, many customers on one device, repeated round-amount cash-outs | S |

### 3.4 Alerts, cases and triage (CAS)

| ID | Requirement | Pri |
| --- | --- | --- |
| CAS-1 | Create alerts from risk decisions with REVIEW or BLOCK_RECOMMENDED and from graph rings | M |
| CAS-2 | Deduplicate and group alerts into cases by entity (account, device, merchant, ring) within a configurable window | M |
| CAS-3 | Compute a triage priority per case from risk score, amount at risk, entity history and typology severity, and show the breakdown | M |
| CAS-4 | Analyst actions: assign, comment, label alert (true fraud, false positive, unclear), recommend freeze, request approval, close with reason | M |
| CAS-5 | Four-eyes approval for freeze recommendations above a configurable amount or any ring-level action; the approver is a different user | M |
| CAS-6 | Execute approved actions against provider-mock (freeze account, hold payout) idempotently and record the result | M |
| CAS-7 | Feedback loop: store labels in `feedback.v1`; compute precision per rule, typology and segment; flag rules whose precision falls below a configurable floor | M |
| CAS-8 | Suggested threshold adjustments from labelled history, shown to the risk manager, never auto-applied | S |
| CAS-9 | Export a suspicious-activity report draft (PDF and JSON) for a case, with timeline, entities and evidence | S |
| CAS-10 | SLA for alert handling (time to first touch) with warnings | S |

### 3.5 Disputes (DSP)

| ID | Requirement | Pri |
| --- | --- | --- |
| DSP-1 | Create a dispute from the customer portal, an analyst, or a provider event; types: FAILED_TXN, WRONG_TRANSFER, MERCHANT_DISPUTE, UNAUTHORIZED, QR_TAMPERING | M |
| DSP-2 | Enforce the filing window (default 45 days from the transaction); late filings are rejected with a clear reason or routed to manual review by permission | M |
| DSP-3 | Auto-refund: for FAILED_TXN where the provider status check confirms failure and money was debited, issue the refund without a customer complaint and notify the customer | M |
| DSP-4 | State machine and transitions as in section 9, with every transition recorded | M |
| DSP-5 | SLA clocks: same-provider default 3 working days; overall maximum default 30 days; fraud or tampering investigation default 30 days; warnings at 50, 80 and 100 percent; working days follow the Friday-Saturday weekend and holiday calendar | M |
| DSP-6 | Cross-provider disputes are handed to a simulated NPSB DMS adapter with status sync | M |
| DSP-7 | Evidence: upload files and notes by customer, merchant and officer, with type, size and malware-scan placeholder; evidence is immutable once submitted | M |
| DSP-8 | Merchant response workflow with its own deadline | M |
| DSP-9 | Resolution outcomes: REFUND_FULL, REFUND_PARTIAL, REJECTED, CHARGEBACK, ESCALATED_TO_ARBITRATION; refunds run through provider-mock with idempotency | M |
| DSP-10 | Link disputes and cases both ways; a confirmed UNAUTHORIZED dispute creates or upgrades a fraud case and feeds labels back to rules | M |
| DSP-11 | Dispute officer queue sorted by deadline risk, with breach prediction flags | M |
| DSP-12 | Bulk actions and templates for common resolutions | S |

### 3.6 Customer portal (CUS)

| ID | Requirement | Pri |
| --- | --- | --- |
| CUS-1 | Sign in with phone and OTP (mock SMS sink in dev) | M |
| CUS-2 | List my transactions (synthetic) and start a dispute from one in under four taps | M |
| CUS-3 | Track a complaint with timeline, current status, next deadline and what is needed from me | M |
| CUS-4 | Upload evidence from a phone camera or gallery with size limits | M |
| CUS-5 | Bengali and English UI, large text option, low-bandwidth mode | M |
| CUS-6 | Safety tips and "report a suspicious call or message" shortcut that creates a UNAUTHORIZED or phishing report | S |

### 3.7 Simulator (SIM)

| ID | Requirement | Pri |
| --- | --- | --- |
| SIM-1 | Generate a reproducible synthetic population (seeded) of customers, agents and merchants with realistic Bangladesh-style attributes (names, phone formats, districts) and no real data | M |
| SIM-2 | Generate normal behavior: salary, P2P family transfers, bill payment, merchant QR, cash-in and cash-out patterns by segment | M |
| SIM-3 | Inject labelled fraud typologies (section 8): pass-through mules, account takeover, QR cash-out abuse, structuring, agent anomaly, plus network rings | M |
| SIM-4 | Inject failed-transaction scenarios (debited but failed) and dispute-worthy scenarios for the dispute flow | M |
| SIM-5 | Control API and console: start, stop, set rate, add a scenario, reset with seed | M |
| SIM-6 | Output a ground-truth label file for evaluation | M |

### 3.8 Evaluation and analytics (EVL)

| ID | Requirement | Pri |
| --- | --- | --- |
| EVL-1 | Evaluation harness: replay a seeded dataset, compute precision, recall, F1, false-positive rate, alert volume, cases, detection delay per typology | M |
| EVL-2 | Compare configurations: baseline rules, rules plus graph, rules plus graph plus grouping, plus shadow model | M |
| EVL-3 | Publish a reproducible report (Markdown and charts) into the repo | M |
| EVL-4 | Dashboard KPIs: alerts, cases, false-positive rate by rule, prevented loss (simulated), dispute SLA compliance, mean time to resolve, refunds issued automatically | M |
| EVL-5 | Drill-down from any KPI to the underlying cases or disputes | S |

### 3.9 Model service (ML) (optional)

| ID | Requirement | Pri |
| --- | --- | --- |
| ML-1 | Train a gradient-boosted model offline on simulator data; export features, model card and metrics | S |
| ML-2 | Serve scores in shadow mode; compare with rules in the evaluation harness; promote only through an approved rule-version change | S |

### 3.10 Administration, security and audit (ADM, AUD)

| ID | Requirement | Pri |
| --- | --- | --- |
| ADM-1 | Users and roles: ANALYST, APPROVER, DISPUTE_OFFICER, RISK_MANAGER, COMPLIANCE, ADMIN, MERCHANT, CUSTOMER | M |
| ADM-2 | TOTP for staff; short-lived tokens; refresh rotation; account lockout | M |
| ADM-3 | Configuration screens for rules, thresholds, SLA durations, holiday calendar, templates, with change preview | M |
| ADM-4 | Feature flags | S |
| AUD-1 | Append-only audit log of privileged actions and decisions with actor, time, IP, before and after values; searchable by admin and compliance | M |
| AUD-2 | Data retention and masking policy: mask phone numbers and NIDs in UI by default; reveal requires permission and is audited | M |

### 3.11 Notifications (NTF)

| ID | Requirement | Pri |
| --- | --- | --- |
| NTF-1 | Notify customers at dispute creation, evidence request, status change, deadline risk and resolution, in Bengali or English by preference | M |
| NTF-2 | Notify staff of SLA warnings and approval requests in-app and by mock email | S |

---

## 4. Data requirements

### 4.1 Core tables (per service)

| Service | Tables |
| --- | --- |
| ingest | `raw_events`, `normalized_events`, `dead_letters`, `idempotency_keys`, `outbox_events` |
| risk | `rules`, `rule_versions`, `rule_approvals`, `lists` (allow and block), `decisions`, `decision_reasons`, `shadow_results`, `processed_events`, `outbox_events` |
| graph | `entities`, `edges`, `rings`, `ring_members`, `graph_jobs` |
| case | `alerts`, `cases`, `case_alerts`, `case_actions`, `labels`, `rule_quality`, `approvals`, `processed_events`, `outbox_events` |
| dispute | `disputes`, `dispute_events`, `evidence`, `sla_clocks`, `calendar_holidays`, `resolutions`, `refund_requests`, `outbox_events` |
| provider-mock | `accounts`, `transactions`, `holds`, `freezes`, `refunds` |
| simulator | `runs`, `scenarios`, `ground_truth` |
| identity | `users`, `roles`, `sessions`, `totp_secrets`, `api_keys`, `audit_log` |

### 4.2 Rules

- Amounts are BIGINT paisa. Times are timestamptz stored in UTC, shown in Asia/Dhaka.
- Phone numbers and NIDs are stored encrypted (field level) and masked in logs and UI.
- Decisions store the rule versions and feature snapshot used, so any score can be replayed.
- `audit_log`, `dispute_events` and `evidence` are append-only (database triggers reject update and delete).

---

## 5. External interfaces

### 5.1 REST API (OpenAPI 3.1; problem-details errors; Idempotency-Key on writes)

| Method and path | Purpose | Role |
| --- | --- | --- |
| POST /v1/events/transactions, /batch | Ingest events | Provider API key |
| POST /v1/events/profile | Ingest device and profile events | Provider API key |
| POST /v1/risk/score | Synchronous score | Provider API key |
| GET, POST /v1/risk/rules, /rules/{id}/versions, /versions/{id}/approve, /rollback | Rule management | Risk manager |
| POST /v1/risk/backtests | Back-test a version | Risk manager |
| GET, POST /v1/risk/lists | Allow and block lists | Analyst, Risk manager |
| GET /v1/alerts, GET /v1/cases, GET /v1/cases/{id} | Queues and detail | Analyst |
| POST /v1/cases/{id}/assign, /comments, /labels, /actions, /close | Analyst work | Analyst |
| POST /v1/approvals/{id}/decide | Four-eyes decision | Approver |
| GET /v1/graph/entities/{id}/neighbors | Graph view | Analyst |
| GET /v1/rings, GET /v1/rings/{id} | Rings | Analyst |
| POST /v1/disputes, GET /v1/disputes, GET /v1/disputes/{id} | Disputes | Customer, Officer |
| POST /v1/disputes/{id}/evidence, /respond, /resolve, /escalate | Dispute actions | Customer, Merchant, Officer |
| GET /v1/me/transactions, /me/disputes | Customer portal data | Customer |
| GET /v1/analytics/kpis, /rule-quality, /sla | Dashboards | Staff |
| POST /v1/sim/runs, /sim/runs/{id}/stop, /sim/scenarios | Simulator control | Admin |
| GET /v1/admin/audit, /admin/calendar, /admin/flags | Admin | Admin, Compliance |
| POST /provider-mock/refunds, /freezes, GET /transactions/{id}/status | Simulated provider | Internal |

### 5.2 Events

All events carry event_id, version, occurred_at, trace_id, and are idempotent by event_id. Schemas live in `contracts/` and are validated in CI.

---

## 6. Non-functional requirements

| ID | Area | Requirement | Proven by |
| --- | --- | --- | --- |
| NFR-1 | Latency | Risk decision p95 under 100 ms, p99 under 250 ms at 1,000 events per second on a laptop-class cluster | k6 and Gatling report |
| NFR-2 | Throughput | Ingest 3,000 events per second sustained for 10 minutes without loss; document the bottleneck found | Load test report |
| NFR-3 | Correctness | No duplicate decisions or duplicate refunds under retries or consumer restarts | Replay and chaos tests |
| NFR-4 | Idempotency | Same key and same body returns the original result; same key with a different body returns 422 | Integration tests |
| NFR-5 | Resilience | Risk-service degrades to ALLOW_WITH_FLAG when Redis or model is down; killing any service loses no accepted event | Chaos tests |
| NFR-6 | Explainability | 100 percent of decisions have reason codes and a replayable feature snapshot | Automated check |
| NFR-7 | Auditability | All privileged actions are in the audit log; the log cannot be altered | DB trigger tests |
| NFR-8 | Security | OWASP ASVS L2 targets; TLS; secrets outside code; rate limiting; RBAC enforced server side; four-eyes enforced server side | ZAP, dependency and secret scans, RBAC test matrix |
| NFR-9 | Privacy | PII encrypted at rest and masked by default; synthetic data only | Unit tests and log scan |
| NFR-10 | Observability | Structured logs with trace ids; metrics for lag, decision latency, alert volume, SLA breaches; dashboards as code | Grafana JSON committed |
| NFR-11 | Quality | 80 percent line coverage on risk, case and dispute modules; Playwright flows for each role | CI gates |
| NFR-12 | Accessibility and UX | WCAG 2.2 AA; customer portal usable on a low-end Android on slow mobile data; initial JavaScript under 200 KB per route for the customer portal | Lighthouse CI and axe |
| NFR-13 | Localization | Full Bengali and English; Bengali digits option; taka formatting with lakh and crore grouping; Asia/Dhaka time | i18n tests |
| NFR-14 | Portability | `docker compose up` starts the full system with seeded demo data; Kubernetes manifests provided | CI smoke test |
| NFR-15 | Reproducibility | Same seed gives identical datasets and evaluation metrics | Evaluation test |

---

## 7. User interface requirements

### 7.1 Console app (analyst, approver, dispute officer, risk manager, compliance, admin)

| Area | Screens |
| --- | --- |
| Overview | KPI wall, live alert stream, SLA risk panel |
| Alerts and cases | Priority queue, case detail with timeline, reason codes with feature values, entity card, graph view, actions, comments, approvals |
| Graph | Entity explorer, ring view, evidence path |
| Rules | Rule list, version history, diff, back-test, shadow results, approve, rollback |
| Disputes | SLA-ordered queue, dispute detail, evidence viewer, resolution forms, merchant response status |
| Insights | False-positive by rule, typology trends, prevented loss, time to resolve, SLA compliance, simulator evaluation comparison |
| Simulator | Run control, scenario injection, live throughput |
| Admin | Users and roles, thresholds, SLA settings, holiday calendar, templates, audit log, feature flags |

### 7.2 Customer portal

Sign-in, my transactions, start dispute wizard, evidence upload, complaint tracker with timeline and next deadline, safety tips, language switch.

### 7.3 General

Responsive, keyboard-friendly, dark mode in console, loading and error states everywhere, no secrets or tokens in localStorage, CSP headers, every destructive action confirmed with a typed reason.

---

## 8. Fraud typology and rule catalog

Parameters below are illustrative defaults for the simulator. They live in the database, not in code.

| Rule | Typology | Logic (summary) | Key features |
| --- | --- | --- | --- |
| R01 Pass-through mule | Mule account | Many unrelated senders in a short window, then most of the inflow leaves quickly by cash-out or transfer | distinct_senders_1h, inflow_1h, outflow_ratio_30m, account_age |
| R02 Young account burst | Mule or fake account | Account under 7 days old receives more than a configured amount or count | account_age, inflow_24h, kyc_tier |
| R03 QR cash-substitute | Bangla QR cash-out abuse | Merchant QR receives large round-amount payments from repeat payers, near the daily cash-out limit, with settlement to bank and no plausible goods pattern | round_amount_ratio, payer_repeat_rate, near_limit_ratio, merchant_category_mismatch, settlement_ratio |
| R04 Account takeover | OTP phishing or SIM swap | New device or SIM change or PIN reset followed within 24 hours by high-value drain to a new beneficiary | device_new, sim_change_24h, new_beneficiary, balance_drain_ratio |
| R05 Structuring | Limit evasion | Several transactions just under a limit within a window | near_limit_count_24h, sum_24h |
| R06 Agent anomaly | Agent misuse | Agent cash-out velocity far above peer baseline, many customers on one device, repeated round amounts | zscore_cashout_velocity, customers_per_device, round_ratio |
| R07 Dormant wake-up | Takeover or mule | Long-idle account suddenly active at high value | idle_days, amount, new_device |
| R08 Shared device or listed entity | Network link | Device or phone linked to a blocklisted or ringed entity | list_hit, ring_member |
| R09 Ring score | Network | Graph service ring score above threshold | ring_score, ring_size, fan_in_out_time |
| R10 Failed-debit watch | Operational | Debited but failed or pending beyond a threshold, creating a dispute candidate | status, age_minutes |

Reason codes are stable strings, for example `R01_FAST_PASS_THROUGH`, each with a Bengali and English human explanation.

---

## 9. Dispute state machine and SLA

### 9.1 States

`OPENED` → `TRIAGED` → (`AWAITING_CUSTOMER` | `AWAITING_MERCHANT` | `UNDER_REVIEW`) → (`RESOLVED_REFUND` | `RESOLVED_REJECTED` | `ESCALATED_CROSS_PROVIDER` | `ESCALATED_ARBITRATION`) → `CLOSED`.
Auto path for confirmed failed debit: `OPENED` → `RESOLVED_REFUND` → `CLOSED` with no customer action.

### 9.2 Transition rules

| From | Event | To | Side effects |
| --- | --- | --- | --- |
| OPENED | Triage completes | TRIAGED | Assign officer, start clocks |
| TRIAGED | Evidence needed from customer | AWAITING_CUSTOMER | Notify, pause only the customer-response sub-clock |
| TRIAGED | Merchant involved | AWAITING_MERCHANT | Notify merchant, set merchant deadline |
| any waiting | Evidence received or deadline passed | UNDER_REVIEW | Notify officer |
| UNDER_REVIEW | Provider status confirms failure | RESOLVED_REFUND | Refund through provider-mock, notify |
| UNDER_REVIEW | Different provider involved | ESCALATED_CROSS_PROVIDER | Send to simulated NPSB DMS |
| UNDER_REVIEW | Fraud confirmed | RESOLVED_REFUND or ESCALATED_ARBITRATION | Create or upgrade case, label rules |
| UNDER_REVIEW | Claim not supported | RESOLVED_REJECTED | Reason required |
| RESOLVED_* | Customer notified | CLOSED | Final audit entry |

### 9.3 Clocks

| Clock | Default | Source (as reported) |
| --- | --- | --- |
| Filing window | 45 days from transaction | Dhaka Tribune report on Bangladesh Bank guideline |
| Same-provider resolution | 3 working days | same |
| Overall maximum including chargeback and arbitration | 30 days | same |
| Fraud or tampering investigation | 30 days | same |

Warnings fire at 50, 80 and 100 percent of each clock. All values are admin-configurable and the source note must be shown in the admin screen.

---

## 10. Evaluation plan

1. Generate a seeded dataset: 100,000 customers, 2,000 agents, 5,000 merchants, 30 simulated days; inject fraud typologies at known rates, including 20 mule rings and 50 QR cash-substitute merchants.
2. Replay through the platform in four configurations: (A) single-event rules, (B) A plus graph, (C) B plus alert grouping, (D) C plus shadow model.
3. Report per typology: precision, recall, F1, detection delay, alerts per true fraud, and analyst-minutes saved using a stated handling-time model.
4. Target on synthetic data (not a claim about real data): grouping cuts analyst-facing alerts by at least 40 percent with recall loss of 2 points or less; QR cash-substitute and ring recall improve over baseline. Report the real numbers whatever they are.
5. Disputes: simulate 5,000 disputes; report SLA compliance, auto-refund share, breach prediction accuracy and mean time to resolve.
6. Publish `docs/evaluation.md` with method, seeds, charts and limitations.

---

## 11. Acceptance criteria (release v1.0)

1. `docker compose up` starts the system and seeds demo data; the demo script in `docs/demo.md` runs end to end.
2. A simulated mule ring is detected, grouped into one case, shown on the graph, approved by a second analyst, frozen in provider-mock, and the action appears in the audit log.
3. A QR cash-substitute merchant is flagged with reason codes and feature values visible to the analyst.
4. A failed-debit transaction is auto-refunded with no complaint and the customer sees the result in the portal.
5. A customer dispute shows correct deadlines on both the customer tracker and the officer queue, with Friday and Saturday excluded from working days.
6. Analyst labels change per-rule precision on the dashboard.
7. A rule change is approved by a second user, takes effect without redeploy, and can be rolled back.
8. Load, chaos and evaluation reports are committed and reproducible.
9. Security scans and the RBAC matrix test pass in CI.
10. Every Must requirement in section 3 maps to at least one passing test (`docs/traceability.md`).

---

## 12. Risks and mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Synthetic data too easy, results look inflated | Credibility | Add noise and hard negatives (legitimate high-volume merchants, salary days, festival spikes); report limitations |
| Regulatory details reported secondhand | Wrong SLA | Configurable clocks with source notes; verify against the circular |
| Scope too large for one person | Delays | Build in phases; Must items first; model service optional |
| Rule thresholds overfit simulator | Weak realism | Back-test across seeds; keep thresholds in data |
| Latency target missed | NFR failure | Redis feature design, batch reads, profile early |
| Misuse of freeze action | Customer harm | Four-eyes, reasons, audit, undo path |
| AI-generated code not understood | Interview failure | Read every module; ADRs; own the design |

---

## 13. Phase map and traceability

| Phase | Delivers | Requirements |
| --- | --- | --- |
| B0 / F0 | Foundation, CI, design system, mocks | NFR-11, NFR-14 |
| B1 | Simulator and provider-mock | SIM-1 to SIM-6, DSP-3 (provider side) |
| B2 | Ingestion and contracts | ING-1 to ING-6 |
| B3 | Risk engine | RSK-1 to RSK-8, R01 to R10 |
| B4 | Graph service | GRP-1 to GRP-5 |
| B5 | Cases, triage, feedback | CAS-1 to CAS-10 |
| B6 | Disputes and SLA | DSP-1 to DSP-12, NTF-1, NTF-2 |
| B7 | Identity, audit, admin, analytics | ADM-*, AUD-*, EVL-4, EVL-5 |
| B8 | Evaluation and model | EVL-1 to EVL-3, ML-1, ML-2 |
| B9 | Production readiness | NFR-1 to NFR-10 |
| F1 | Analyst console | CAS-*, GRP-4, RSK-4 UI |
| F2 | Dispute desk | DSP-* UI |
| F3 | Customer portal | CUS-* |
| F4 | Insights, simulator, admin | EVL-4, SIM-5, ADM-3 |
| F5 | Quality and demo | NFR-12, NFR-13 |

---

## 14. Sources

- [Dhaka Tribune: Bangladesh Bank dispute resolution guidelines for Bangla QR](https://www.dhakatribune.com/business/banks/420879/bb-issues-guidelines-for-dispute-resolution-in)
- [Observer BD: Tk 21,000cr vanishes in MFS fraud](https://www.observerbd.com/news/575239)
- [ScienceDirect: Bangladesh-facing hybrid investment fraud](https://www.sciencedirect.com/science/article/pii/S2949791426000461)
- [Financial Express: Bangla QR misuse](https://today.thefinancialexpress.com.bd/last-page/bangla-qr-misuse-raises-concerns-over-mfs-sustainability-1789410537)
- [TBS: How the abuse of Bangla QR threatens the MFS ecosystem](https://www.tbsnews.net/bangladesh/how-abuse-bangla-qr-threatens-countrys-mfs-ecosystem-1538591)
- [Observer BD: bKash complaints](https://observerbd.com/news/570386)
- [IPA: Bangladesh Consumer Protection in Digital Finance Survey](https://poverty-action.org/bangladesh-consumer-protection-digital-finance-survey)
- Full list with global sources in `RESEARCH.md`.
