# Astha (আস্থা): Real-Time Fraud & Dispute Operations Platform

Astha is a production-grade, real-time fraud mitigation and dispute operations platform engineered for Bangladeshi mobile financial services (bKash, Nagad, Rocket, Upay). It is designed to score high-velocity transactions under tight latency budgets (p95 < 100ms), detect synthetic identities and mule rings, and enforce strict regulatory governance (Bangladesh Bank four-eyes approvals, audited PII access, and bilingual English/Bengali localization).

---

## 1. System Architecture

```text
astha/
├── contracts/                       <- JSON Schemas for txn.events.v1, profile.events.v1, risk.decisions.v1, alerts.v1
├── deploy/                          <- docker-compose.yml (Postgres 16, Redis 7, Kafka KRaft, MinIO)
├── docs/                            <- SRS.md, RESEARCH.md, traceability.md, screens.md, adr/
├── packages/
│   ├── api-contract/                <- OpenAPI 3.1 specification (openapi.yaml)
│   ├── api-client/                  <- Typed TypeScript client & schemas
│   ├── i18n/                        <- Bengali (বাংলা) & English catalogs, taka formatting (Lakh/Crore)
│   ├── mocks/                       <- MSW mock handlers for frontend development & testing
│   └── ui/                          <- Shared Design System (RiskBadge, MaskedValue, GraphCanvas, etc.)
├── services/
│   ├── platform-lib/                <- ProblemDetails, IdempotencyFilter, Transactional Outbox, PiiMasker, Money
│   └── risk-service/                <- Real-time Risk Engine, R01-R10 Rules, Redis Feature Store, Four-Eyes Desk
└── apps/
    └── console/                     <- Analyst & Approver Console (Next.js 14 App Router, TailwindCSS)
```

---

## 2. Implemented Phases & Coverage

### Phase B3: Risk Engine (`services/risk-service`)
- **Core Requirements**: `RSK-1` to `RSK-8`, `R01` to `R10`, `NFR-1`, `NFR-5`, `NFR-6`, `NFR-10`, `NFR-11`, `NFR-14`.
- **Key Capabilities**:
  - **Rule Catalog (R01-R10)**: Pass-through mule, young account bursts, Bangla QR cash-substitutes, account takeovers, structuring near limits, agent velocity anomalies, dormant account wakeups, shared devices, graph mule rings, and failed debit watches.
  - **Redis Feature Store**: Rolling counters, velocity sums, and ratio tracking with automatic fallback to in-memory profile cache under Redis timeout (`NFR-5`).
  - **Deterministic Decisioning**: 0-100 normalized risk score, typed decision reason codes, shadow-mode candidate evaluation, and transactional outbox publishing (`risk.decisions.v1`, `alerts.v1`).
  - **Four-Eyes Rule Governance**: Rule activation and deactivation requires distinct approver approval; maker self-approval is strictly prohibited.
  - **30-Day Historical Backtesting**: Evaluates candidate rule parameter changes against historical transaction snapshots before deployment.

### Phase F1: Analyst Console (`apps/console`)
- **Core Requirements**: `CAS-1` to `CAS-10`, `GRP-4`, `RSK-4`, `RSK-5`, `RSK-8`, `NFR-11`, `NFR-13`, `NFR-14`.
- **Key Capabilities**:
  - **Triage Queue**: Prioritized alert queue with risk badges, SLA countdowns, and multi-channel filtering (bKash, Nagad, Rocket, Upay).
  - **Case Investigation Dossier**: Complete transaction telemetry, rule triggers, bilingual explanations, and audited PII masking (MSISDN, NID) with 30s auto-hide timer.
  - **Interactive Entity Graph**: SVG radial topology canvas displaying mule account clusters, shared devices, and highlighted evidence paths.
  - **Four-Eyes Approvals Desk**: Governance workflow for irreversible account freezes and rule activations.
  - **Bilingual Localization**: Instant switching between English and Bengali (`বাংলা`) with accurate Taka formatting (`৳১,৫০,০০০.০০`).

---

## 3. Verification & Quality Gates

Run all automated verification gates:

```bash
# Backend checks (Java 21, Spring Boot, Unit & Integration Tests)
./gradlew check

# Frontend linting, typechecking, and unit tests
pnpm lint
pnpm typecheck
pnpm test

# Frontend Playwright End-to-End tests
pnpm e2e

# Infrastructure smoke test
docker compose up -d
```

---

## 4. Documentation Links

- [Software Requirements Specification (`docs/SRS.md`)](file:///docs/SRS.md)
- [Requirements Traceability Matrix (`docs/traceability.md`)](file:///docs/traceability.md)
- [Analyst Console Screens & States (`docs/screens.md`)](file:///docs/screens.md)
- [Architecture Decision Records (`docs/adr/`)](file:///docs/adr/)
- [OpenAPI 3.1 Contract (`packages/api-contract/openapi.yaml`)](file:///packages/api-contract/openapi.yaml)
