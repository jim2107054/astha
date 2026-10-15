# Astha: Frontend Team Prompts

For Claude Code or Antigravity. Read `README.md` for the workflow. Put `SRS.md` at `docs/SRS.md` and the backend's `openapi.yaml` at `packages/api-contract/openapi.yaml`.

## How to use

1. Create the monorepo, add `docs/SRS.md`, and save the shared brief below as `CLAUDE.md` in the repo root (Antigravity: project rules).
2. Run one phase at a time. Phase F0 builds a mock server from the OpenAPI file so every screen works with realistic data before the backend is ready.
3. After each phase: run lint, typecheck, unit tests and Playwright; open the app and click through; commit.
4. If the agent drifts: "Re-read CLAUDE.md and docs/SRS.md, list the requirement IDs you are implementing, then continue."

## Shared brief (save as CLAUDE.md)

```text
Project: Astha frontend. pnpm + Turborepo monorepo:
apps/console (analysts, approvers, dispute officers, risk managers, compliance, admin) and apps/customer (customer and merchant complaint portal), packages/ui (design system), packages/api-client (generated), packages/i18n, packages/config, packages/mocks (MSW).
Read docs/SRS.md. Implement by requirement ID (CAS-, DSP-, CUS-, RSK-, GRP-, EVL-, SIM-, ADM-, AUD-, NFR-). Put IDs in commit messages and test names.

Stack: Next.js App Router, React, TypeScript (strict, no any), Tailwind, shadcn/ui, TanStack Query and TanStack Table, React Hook Form + Zod, next-intl, Recharts (or visx for the graph view: use a force-directed graph library such as react-force-graph or Cytoscape.js), Vitest + Testing Library, Playwright, Storybook, MSW.

Rules:
1. The API client is generated from packages/api-contract/openapi.yaml. Never hand-write fetch calls to the backend.
2. Money is integer paisa; one formatMoney helper shows taka with lakh and crore grouping and Bengali digits when the locale is bn. Dates show in Asia/Dhaka.
3. Every state-changing request sends an Idempotency-Key created once per form attempt and reused on retry.
4. All strings use next-intl with bn and en catalogs. No hard-coded text. Customer portal defaults to Bengali.
5. Every screen has loading, empty, error, offline and permission-denied states. Show problem-details messages in plain language.
6. Role-based UI: hide or disable actions the user's role cannot perform and say why; the server remains the authority.
7. PII is masked by default. A reveal action asks for a reason, calls the API, and shows the value for a short time only.
8. Irreversible or money-affecting actions (freeze, reject, refund, rule approval) use a confirm dialog with a typed reason.
9. Security: tokens in httpOnly cookies through a BFF route, no tokens in localStorage, CSP headers, sanitize all rendered user text, no PII in logs or analytics.
10. Accessibility: WCAG 2.2 AA, keyboard reachable, visible focus, labels on every input, reduced motion respected, color is never the only signal (risk levels also use text and icons).
11. Performance: customer portal under 200 KB initial JavaScript per route; console uses route-level code splitting and virtualized tables.
12. Every component has a Storybook story; every user flow has a Playwright test.
Before finishing a task: pnpm lint, pnpm typecheck, pnpm test, pnpm e2e, and update docs/screens.md.
```

---

## Phase F0: Foundation, design system, mocks

Requirements: NFR-11, NFR-13, NFR-14.

```text
Read CLAUDE.md and docs/SRS.md.

1. Set up the monorepo, TypeScript project references, ESLint, Prettier, Husky, GitHub Actions (lint, typecheck, test, build, Playwright, Lighthouse CI).
2. Build packages/ui from design tokens. Two modes: a dense, calm console theme (light and dark) and a friendly, large-text customer theme. Palette: neutral greys with a deep teal primary; clear semantic colors for risk levels (low, medium, high, critical) that always pair color with a label and icon. Bengali-friendly font stack (Noto Sans Bengali plus Inter), 4 pt spacing.
3. Components: Button, Input, PhoneInput, OtpInput, MoneyInput, Amount, MaskedValue (with reveal-with-reason), RiskBadge, ReasonCodeList, StatusBadge, SlaTimer (shows deadline, remaining working time, warning level), Timeline, DataTable (sort, filter, column visibility, virtualization, saved filters), FilterBar, Drawer, Dialog, ConfirmWithReason, Toast, Tabs, Stepper, FileDropzone with camera capture, EmptyState, Skeleton, Chart wrappers, GraphCanvas.
4. Generate packages/api-client from the OpenAPI file and start MSW handlers with realistic fixtures: a few hundred alerts and cases, rings, disputes at various SLA stages, rules with versions, KPIs. Include slow, error and permission-denied variants.
5. next-intl with bn and en, formatMoney, formatDate, Bengali digits option, working-day helper that mirrors the Friday and Saturday weekend for display only.
6. Storybook with accessibility checks and a docs page for each component.

Done when Storybook builds, a sample page renders in both languages in both themes with mocked data, and CI is green.
```

## Phase F1: Analyst console

Requirements: CAS-1 to CAS-10, GRP-4, RSK-4 (UI), RSK-5, RSK-8.

```text
Build apps/console: auth shell and the fraud analyst experience.

- Sign-in with email, password and TOTP; role-aware navigation; session expiry handling.
- Overview: live alert stream (server-sent events), KPI tiles, SLA risk panel.
- Alert and case queue: priority-ordered table with filters (typology, rule, status, assignee, amount, age), triage score with a hover or drawer showing its breakdown, bulk assign, saved views, keyboard shortcuts.
- Case detail: header with entity card (masked PII), score and decision, timeline of alerts and actions, reason codes with their feature values side by side with a plain-language explanation in the user's language, linked disputes, comments, label buttons (true fraud, false positive, unclear) with required reason on override, recommend freeze with amount at risk, approval request status.
- Graph view: entity explorer (depth 1 to 3, time and amount filters), ring view highlighting the evidence path, click a node to open its card, export an image.
- Approver view: pending approvals with context and a different-user check shown in the UI; approve or reject with a typed reason.
- Rules area: rule list, version history, diff view between versions, back-test result panel (alerts, precision, recall), shadow comparison, approve and rollback with confirm-with-reason; allowlist and blocklist management.
- Case report export (PDF and JSON preview).

Playwright flows: analyst works a mule-ring case end to end (open, inspect graph, label, recommend freeze), approver approves, audit entry visible; false-positive labelling updates the rule quality widget; RBAC checks for each role.
```

## Phase F2: Dispute desk

Requirements: DSP-1 to DSP-12 (UI), NTF-2.

```text
Add the dispute desk to apps/console.

- SLA-ordered queue with SlaTimer per row, breach prediction flag, filters by type, stage, provider, officer, merchant; bulk actions and resolution templates.
- Dispute detail: transaction summary, status timeline with every transition, clocks panel (filing window, same-provider clock, overall clock, merchant response clock) with working-day explanations and a visible note that durations come from configuration, evidence viewer (images, PDFs, notes, checksum and uploader), customer and merchant responses, linked fraud case, refund status.
- Actions: request evidence, send to merchant, hand to cross-provider DMS (shows sync status), resolve with outcome (full refund, partial refund with amount, reject with required reason, chargeback, escalate to arbitration), with confirm-with-reason on money-affecting outcomes.
- Auto-refund view: list of automatically refunded failed transactions with proof of provider status confirmation.
- In-app notifications for SLA warnings.

Playwright flows: officer resolves a failed-transaction dispute with a refund; a dispute crossing a weekend shows correct remaining working time; a late filing routes to manual review; an unauthorized dispute upgrades to a fraud case.
```

## Phase F3: Customer portal

Requirements: CUS-1 to CUS-6, NFR-12, NFR-13.

```text
Build apps/customer. Bengali first, mobile first, installable PWA.

- Sign in with phone and OTP; clear errors; no account enumeration messages.
- Home: recent transactions list (synthetic) with status chips; a prominent "Report a problem" button.
- Start dispute wizard in four taps or fewer: choose transaction, choose what happened (failed but money deducted, sent to wrong number, merchant did not deliver, I did not do this, QR looks tampered), add a short note, add photos from camera or gallery with client-side compression and size limits, confirm. If the transaction is a confirmed failed debit, show that it is being refunded automatically.
- Complaint tracker: plain-language status, a timeline, the next deadline in dates and remaining working days (Friday and Saturday excluded, with a small explanation), what is needed from the customer (upload more evidence), and a notification inbox.
- Merchant mode (role MERCHANT): see disputes about my business, respond with text and evidence before the deadline.
- Safety tips and a "report suspicious call or message" shortcut.
- Language switch bn and en, large text option, low-bandwidth mode (no images preloaded, lighter lists), offline shell with a banner, slow-network and retry handling, double-tap protection.

Budgets: initial JavaScript under 200 KB per route; Lighthouse performance 90 or more on a mobile profile with throttled 3G; axe has zero serious issues.
Playwright flows: file a dispute with evidence, track it to resolution, auto-refund case, switch language, offline then online recovery.
```

## Phase F4: Insights, simulator control, admin

Requirements: EVL-4, EVL-5, SIM-5, ADM-1 to ADM-4, AUD-1, AUD-2.

```text
Add to apps/console.

- Insights: false-positive rate by rule (with trend and the precision floor line), typology volumes, alert-to-case reduction, prevented loss (simulated), dispute SLA compliance, mean time to resolve, auto-refund share, evaluation comparison page showing configurations A to D with a clear table and charts, and drill-down from any chart element to the underlying cases or disputes. Charts must have accessible tables and labels.
- Simulator: start, stop, rate slider, scenario injector (mule ring, QR cash-substitute merchant, takeover wave, failed-debit batch), live throughput and decision latency gauges, reset with seed.
- Admin: users and roles, TOTP reset flow, thresholds and rule parameters with change preview, SLA durations with source notes, holiday calendar editor, notification templates in Bengali and English, feature flags.
- Audit log: searchable table with actor, action, entity, before and after diff, export; PII reveal events highlighted.

Playwright flows: admin changes a threshold, it is audited and visible in the audit log; simulator injects a ring and the console shows the new case within seconds; KPI drill-down lands on the right filtered list.
```

## Phase F5: Quality, polish, demo

Requirements: NFR-12, NFR-13, NFR-14.

```text
Raise quality and prepare the demo.

- Run Lighthouse CI and axe on every route; fix regressions to meet the budgets.
- Visual regression snapshots in Storybook; component tests for edge cases (long Bengali names, very large amounts, 100,000-row tables, empty and error states).
- Test with throttled 3G and a low-end Android profile; fix jank.
- Add error boundaries and an error reporting sink; verify no PII in logs or analytics.
- Complete docs/screens.md: every screen, its states, its role access and its Playwright test.
- Build the seeded demo path in docs/demo.md: a 3-minute script that shows (1) the simulator injecting a mule ring, (2) the analyst grouping and graph view, (3) four-eyes freeze, (4) a QR cash-substitute merchant with reason codes, (5) an auto-refunded failed transaction, (6) a customer dispute with deadlines, (7) the rule precision dashboard moving after labels, (8) the evaluation comparison.
- Record screenshots for the README.

Done when every Must UI requirement maps to a passing Playwright test and the demo path runs without manual fixes.
```

## Frontend definition of done

- All Must UI requirements pass Playwright tests and are listed in `docs/screens.md`.
- Customer portal meets the performance and accessibility budgets.
- Bengali and English are complete; no hard-coded strings.
- No tokens in browser storage; no PII in logs or analytics.
