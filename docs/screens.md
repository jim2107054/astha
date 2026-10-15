# Astha Analyst Console — Screen & UI Specification (`docs/screens.md`)

This document catalogs every screen, state, user role authorization, and associated test coverage for the Astha Analyst Console (`apps/console`), implementing phases **F1** and **B3** in compliance with `docs/SRS.md`.

---

## 1. Global Navigation & Shell

- **Route**: `/*`
- **Component**: `apps/console/src/components/Console.tsx`
- **Authorized Roles**: `ANALYST`, `APPROVER`, `RISK_MANAGER`, `AUDITOR`, `OPS`
- **Elements**:
  - **Operator Persona Switcher**: Allows simulated switching between `analyst-sakib` (`ANALYST`), `manager-tamim` (`APPROVER`), and `risk-head` (`RISK_MANAGER`) for role-based testing and governance audits.
  - **Bilingual Language Toggle**: One-click instant toggle between English (`EN`) and Bengali (`বাংলা`) with automatic digit conversion (`০-৯`), currency formatting (টাকা Lakh/Crore), and localized risk labels.
  - **Real-Time Notification Banner**: Notifies operators of active degraded mode operation or emergency alerts.
- **Automated Tests**:
  - `apps/console/tests/analyst-workflow.spec.ts` -> `"Language toggle switches between English and Bengali"`
  - `packages/i18n/src/formatMoney.test.ts`
  - `packages/i18n/src/formatDate.test.ts`

---

## 2. Screen Catalog

### Screen 1: Real-Time Overview & Operations Pulse
- **Navigation**: Tab `"Overview"`
- **Authorized Roles**: All roles
- **SRS Requirements**: `CAS-1`, `CAS-2`, `NFR-5`, `NFR-11`
- **Description**: High-level real-time pulse of the transaction pipeline, fraud alert volumes, active SLA countdowns, and engine health status.
- **Key States**:
  - **Normal Operation**: Green heartbeat, live counts for critical/high/medium triage queues, average latency metric (<20ms p95).
  - **Degraded Mode Active**: Prominent warning banner indicating Redis feature-store fallback to in-memory profile cache; rule evaluations continue safely.
  - **Zero Queue State**: Empty queue celebratory state when all high/critical alerts have been triaged within SLA.
- **Automated Test**: `analyst-workflow.spec.ts`

---

### Screen 2: Fraud Alerts & Triage Queue
- **Navigation**: Tab `"Alerts Queue"`
- **Authorized Roles**: `ANALYST`, `APPROVER`, `RISK_MANAGER`, `AUDITOR`
- **SRS Requirements**: `CAS-1`, `CAS-2`, `CAS-3`, `RSK-1`, `RSK-2`
- **Description**: Priority-ordered alert feed sorted by risk score descending and SLA deadline ascending. Features instant filtering by channel (bKash, Nagad, Rocket, Upay) and severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
- **Key States**:
  - **Default Populated**: Interactive table with risk badge (color + icon + text), customer identifier (masked), channel logo/badge, transaction amount (formatted in Bengali or English), SLA countdown badge, and primary triggered rule tags (e.g., `R01_FAST_PASS_THROUGH`).
  - **Filtered State**: Real-time filtering matching search queries and channel dropdowns.
  - **Selected Row**: Highlights current active investigation, updating context throughout the console.
- **Automated Test**: `analyst-workflow.spec.ts` -> Step 1: `"Verify queue loaded with cases"`

---

### Screen 3: Case Detail & Fraud Investigation Dossier
- **Navigation**: Tab `"Case Detail"` (or clicking any row in the Alerts Queue)
- **Authorized Roles**: `ANALYST`, `APPROVER`, `RISK_MANAGER`, `AUDITOR`
- **SRS Requirements**: `CAS-3`, `CAS-4`, `CAS-5`, `CAS-6`, `CAS-7`, `RSK-6`, `NFR-6`, `NFR-14`
- **Description**: Exhaustive investigative dossier containing transaction telemetry, rule triggers, bilingual explanations, linked accounts, and action controls.
- **Key States**:
  - **PII Masked (Default)**: Sensitive fields (MSISDN `+880****3344`, National ID `1993****8821`) masked with asterisks to prevent shoulder-surfing and unauthorized leakage.
  - **PII Reveal Modal (Audited)**: Operator clicks "Reveal" -> prompt requires mandatory business justification (e.g. "CID inquiry #8821").
  - **PII Unmasked (Temporary 30s)**: Unmasked value displayed alongside a 30-second countdown badge. Automatically re-masks upon expiration. Audit log event generated.
  - **Rule Explanations Card**: Displays each triggered rule ID (R01-R10), typed score impact, and customer-facing / regulatory bilingual explanation in English and Bengali.
  - **Irreversible Action Trigger**: Buttons for "Recommend Freeze", "Close False Positive", and "Add Note".
- **Automated Test**: `analyst-workflow.spec.ts` -> Steps 2-6: `"Analyst works a mule-ring case end-to-end and requests freeze"`

---

### Screen 4: Entity Link Graph Explorer
- **Navigation**: Tab `"Graph Explorer"` (or "Open in Graph Canvas" on Case Detail)
- **Authorized Roles**: `ANALYST`, `APPROVER`, `RISK_MANAGER`, `AUDITOR`
- **SRS Requirements**: `GRP-4`, `CAS-4`, `CAS-7`
- **Description**: Interactive multi-ring topology canvas visualizing suspected mule rings, shared devices, IMEI links, and circular transaction flows.
- **Key States**:
  - **Radial Clustering**: Seed account at center, 1-hop counter-parties on inner ring, 2-hop / shared device entities on outer ring.
  - **High-Risk Evidence Path**: Critical fraud flow paths highlighted in glowing crimson with directional indicators.
  - **Node Selection & Detail Drawer**: Clicking any node opens lateral drawer showing entity ID, risk score, hop distance, and connected transaction amounts.
- **Automated Test**: `analyst-workflow.spec.ts` -> Step 5: `"Inspect reason codes and graph canvas"`

---

### Screen 5: Four-Eyes Governance & Approvals Desk
- **Navigation**: Tab `"Four-Eyes Approvals"`
- **Authorized Roles**: `APPROVER`, `RISK_MANAGER` (`ANALYST` can view but cannot approve)
- **SRS Requirements**: `CAS-8`, `RSK-4`, `RSK-5`, `NFR-11`
- **Description**: Enforcement desk for high-impact and irreversible actions (Account Freeze, Rule Activation, Rule Deactivation, Dispute Auto-Resolution > 5,000 BDT).
- **Key States**:
  - **Maker Self-Approval Prohibited**: When viewing an action submitted by the logged-in user, the "Approve" button is disabled and displays `"Self-approval prohibited (Four-eyes policy)"`.
  - **Distinct Approver Active**: When logged in as `manager-tamim` or `risk-head`, action items display active "Approve Action" and "Reject" buttons.
  - **Approval Completed**: Action transition to `APPROVED` or `REJECTED`, timestamped and signed with operator ID in append-only audit trail.
- **Automated Test**: `analyst-workflow.spec.ts` -> Steps 7-10: `"Self-approval check & Approver action"`

---

### Screen 6: Rule Lifecycle & Backtesting Area
- **Navigation**: Tab `"Rules Area"`
- **Authorized Roles**: `RISK_MANAGER`, `APPROVER`, `AUDITOR`
- **SRS Requirements**: `RSK-4`, `RSK-5`, `RSK-8`, `R01`-`R10`
- **Description**: Comprehensive rules governance console. Lists all 10 core rules (R01-R10), current active versions, thresholds, shadow-mode toggles, and draft parameter tuning with historical backtesting.
- **Key States**:
  - **Rule Catalog View**: Table of active rules with version number, weight, status, and shadow mode badge.
  - **Draft Tuning Form**: Modal allowing risk managers to modify parameters (e.g. `windowMinutes`, `passThroughThresholdPct`).
  - **30-Day Historical Backtest Replay**: Executes draft rule against historical event archive, plotting projected detection rate, false positive rate, and transaction impact before submitting for four-eyes activation.
- **Automated Test**: `apps/console/src/components/Console.test.ts`

---

## 3. UI Accessibility & Design Standards Matrix

| Standard | Implementation | Verification |
|---|---|---|
| **Color Contrast** | WCAG 2.1 AA compliant. Deep teal `#0f766e`, slate backgrounds, high-contrast text `#f8fafc`. | Playwright visual scan |
| **Non-Color Indicators** | All risk badges combine color with distinct icons (`AlertOctagon`, `AlertTriangle`, `Info`, `CheckCircle`) and explicit text labels. | `RiskBadge.tsx` |
| **PII Protection** | Zero plaintext PII in default views; explicit audited unlock dialog; 30s auto-expiry. | `MaskedValue.tsx` |
| **Bilingual Localization** | Complete EN and BN phrase dictionaries; numerals convert to Bengali glyphs (`০-৯`). | `@astha/i18n` unit tests |
| **Audit Trails** | Every unmask, case transition, and approval emits an immutable audit event. | Integration tests |
