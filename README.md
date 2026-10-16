# Astha (আস্থা): Real-Time Fraud & Dispute Operations Platform

Astha is a production-grade, real-time fraud mitigation and dispute operations platform engineered for Bangladeshi mobile financial services (bKash, Nagad, Rocket, Upay). It scores high-velocity transactions under tight latency budgets (p95 < 100ms), detects synthetic identities and mule rings, and enforces strict regulatory governance (Bangladesh Bank four-eyes approvals, audited PII access, Friday/Saturday working-day SLA calculation, and bilingual English/Bengali localization).

---

## 1. Clean Two-Tier Architecture

The repository is organized into cleanly separated top-level `backend/` and `frontend/` directories:

```text
astha/
├── backend/                         <- Java 21 / Spring Boot 3 Multi-Module Architecture
│   ├── docker-compose.yml           <- PostgreSQL 16, Redis 7, Kafka KRaft, MinIO
│   ├── settings.gradle.kts
│   └── services/
│       ├── platform-lib/            <- ProblemDetails, Idempotency, Transactional Outbox, Masking, Money
│       ├── ingest-service/          <- Event ingestion, schema validation, partition key ordering (ING-1..6)
│       ├── provider-mock/           <- MFS provider mock: accounts, balances, idempotent refund & freeze (SIM-1, DSP-3)
│       ├── simulator-service/       <- Seeded synthetic MFS population generator & fraud injectors (SIM-1..6)
│       ├── risk-service/            <- Real-time Risk Engine, R01-R10 Rules, Redis Feature Store, Four-Eyes (RSK-1..8)
│       ├── graph-service/           <- Entity graph store, neighbor traversal up to depth 3, mule rings (GRP-1..5)
│       ├── case-service/            <- Case clustering, triage priority scoring, four-eyes approvals (CAS-1..10)
│       ├── dispute-service/         <- State machine, Bangladesh Bank SLA calendar math, auto-refund (DSP-1..12)
│       └── identity-service/        <- Staff RBAC, customer OTP login, append-only audit log, admin configs (ADM-1..4, AUD-1..2)
│
├── frontend/                        <- Turborepo / pnpm / Next.js 14 App Router Monorepo
│   ├── pnpm-workspace.yaml
│   ├── apps/
│   │   ├── console/                 <- Analyst & Approver Console, Rules Studio & Dispute Desk (Phase F1 & F2)
│   │   └── customer/                <- Customer Complaint PWA (Bengali-first, 4-tap dispute wizard) (Phase F3)
│   └── packages/
│       ├── api-contract/            <- OpenAPI 3.1 specification (openapi.yaml)
│       ├── api-client/              <- Typed TypeScript client & schemas
│       ├── i18n/                    <- Bengali (বাংলা) & English catalogs, taka formatting (Lakh/Crore)
│       ├── mocks/                   <- MSW mock handlers & fixtures for cases, rings, disputes, rules
│       └── ui/                      <- Shared Design System (RiskBadge, MaskedValue, GraphCanvas, etc.)
│
├── contracts/                       <- JSON Schemas for txn.events.v1, profile.events.v1, etc.
└── docs/                            <- SRS.md, RESEARCH.md, traceability.md, screens.md, adr/
```

---

## 2. Implemented Capabilities & Verification

### Backend Services (Java 21 / Spring Boot 3)
1. **`ingest-service` (ING-1 to ING-6)**: Validates incoming MFS events against JSON schema, persists to transactional outbox, partitioned sequentially per account.
2. **`provider-mock` (SIM-1, DSP-3)**: Simulates core banking/MFS ledger with balances, idempotent refunds, and account freezes.
3. **`simulator-service` (SIM-1 to SIM-6)**: Deterministic seeded generation of Bangladeshi population (names, phone formats, KYC tiers) and typology injectors (mule chains, near-limit structuring, hard negatives).
4. **`risk-service` (RSK-1 to RSK-8, R01-R10)**: Real-time decisioning with Redis feature store, R01-R10 rule catalog, 0-100 scoring with bilingual reasons, and four-eyes rule lifecycle.
5. **`graph-service` (GRP-1 to GRP-5)**: Entity graph (accounts, devices, phones, agents), neighbor traversal up to depth 3, and automated fan-in fast fan-out mule ring detection.
6. **`case-service` (CAS-1 to CAS-10)**: Automated alert grouping by entity, priority triage scoring, analyst workflow, and server-enforced four-eyes maker-checker.
7. **`dispute-service` (DSP-1 to DSP-12)**: Dispute state machine, automated debited-but-failed refund worker, and Bangladesh Bank regulatory SLA engine (Friday & Saturday weekends excluded).
8. **`identity-service` (ADM-1 to AUD-2)**: Phone OTP login, staff RBAC roles, centralized append-only audit log, and audited PII reveal with mandatory justification.

### Frontend Applications (Next.js 14 / TypeScript)
1. **`apps/console` (Phase F1 & F2)**:
   - Priority-ordered triage queue with multi-filter search.
   - Case dossier with audited PII masking (`MaskedValue` with reason modal and 30s auto-hide timer).
   - Interactive entity graph visualizer (`GraphCanvas`).
   - Four-Eyes Approvals Desk with maker-checker self-approval prevention.
   - Rules management studio with version diffs and 30-day historical replay backtesting.
   - Dispute Desk with SLA timers, breach risk badges, and full refund resolution.
2. **`apps/customer` (Phase F3)**:
   - Installable, mobile-first PWA with Bengali-first design.
   - Phone OTP login (mock OTP `123456`).
   - Recent transactions with status badges.
   - 4-Tap Dispute Filing Wizard with automated debited-but-failed notification.
   - Live dispute status tracker with Bangladesh Bank regulatory 3-day SLA countdown.

---

## 3. Running & Verifying the Platform

### Running Backend Tests
```powershell
cd backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\gradlew.bat check
```
*All 7 backend services and `platform-lib` execute their unit and integration tests and pass 100%.*

### Running Frontend Tests
```powershell
# Analyst Console (Phase F1 & F2)
cd frontend/apps/console
pnpm typecheck
pnpm test
pnpm e2e

# Customer Complaint Portal (Phase F3)
cd frontend/apps/customer
pnpm typecheck
pnpm e2e
```
*All TypeScript typechecks, Vitest unit tests, and Playwright Chromium E2E flows pass 100%.*

### Starting Local Development
```powershell
# In terminal 1 (Analyst Console):
cd frontend/apps/console
pnpm dev

# In terminal 2 (Customer Complaint Portal):
cd frontend/apps/customer
pnpm dev
```
Open [http://localhost:3000](http://localhost:3000) for the Analyst Console and [http://localhost:3001](http://localhost:3001) for the Customer Portal.
