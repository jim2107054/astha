# Astha: Engineering Standards & Project Rules

Project: Astha, a real-time fraud and dispute operations platform for mobile financial services in Bangladesh. All data is synthetic. No real money, no real PII.
Read docs/SRS.md first. Implement by requirement ID (ING-, RSK-, GRP-, CAS-, DSP-, SIM-, EVL-, ML-, ADM-, AUD-, NTF-, NFR-). Put IDs in commit messages and test names.

Stack: Java 21, Spring Boot 3, Gradle multi-module, PostgreSQL 16 with Flyway (one schema per service), Kafka (KRaft), Redis, Testcontainers, JUnit 5, OpenAPI 3.1, Docker Compose, Kubernetes manifests, OpenTelemetry, Prometheus, Grafana. Optional model-service in Python 3.12 with FastAPI and LightGBM.
Services: api-gateway, identity-service, ingest-service, risk-service, graph-service, case-service, dispute-service, notification-service, provider-mock, simulator-service, analytics (read API), optional model-service.

## Backend Rules

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

## Frontend (apps/ and packages/)

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
