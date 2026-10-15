# Astha: Build Pack

A real-time fraud and dispute operations platform for Bangladeshi mobile-money and payment providers, with a synthetic-data simulator. This folder is everything you need to build it with Claude Code or Antigravity.

| File | What it is |
| --- | --- |
| `RESEARCH.md` | Problems fintechs face (global and Bangladesh), scoring, and why this one was chosen |
| `SRS.md` | Software Requirements Specification with requirement IDs, rules, state machine, evaluation plan, acceptance criteria |
| `BACKEND_PROMPTS.md` | Shared brief plus phases B0 to B9 |
| `FRONTEND_PROMPTS.md` | Shared brief plus phases F0 to F5 |

## Folder structure

Build Pack (these five files) and the planned repository the agents will create:

```text
astha/                              <- one monorepo (or split into astha-backend and astha-frontend)
├── README.md                       <- Build Pack index (this file); replace with the project README later
├── CLAUDE.md                       <- shared brief (copy from BACKEND_PROMPTS.md, add frontend rules for apps/)
├── docs/
│   ├── SRS.md                      <- Software Requirements Specification
│   ├── RESEARCH.md                 <- problem research and decision
│   ├── prompts/
│   │   ├── BACKEND_PROMPTS.md      <- phases B0 to B9
│   │   └── FRONTEND_PROMPTS.md     <- phases F0 to F5
│   ├── adr/                        <- architecture decision records (agents write these)
│   ├── simulator.md, performance.md, evaluation.md, threat-model.md,
│   │   traceability.md, demo.md, screens.md   <- produced by the phases
│   └── runbooks/
├── contracts/                      <- event JSON Schemas, validated in CI
├── services/                       <- Java 21, Spring Boot, Gradle multi-module
│   ├── platform-lib/               <- problem details, idempotency, outbox, tracing, security
│   ├── api-gateway/
│   ├── identity-service/
│   ├── ingest-service/
│   ├── risk-service/
│   ├── graph-service/
│   ├── case-service/
│   ├── dispute-service/
│   ├── notification-service/
│   ├── provider-mock/
│   ├── simulator-service/
│   ├── analytics/
│   └── model-service/              <- optional, Python FastAPI
├── eval/                           <- evaluation harness and reports
├── apps/                           <- pnpm + Turborepo
│   ├── console/                    <- analysts, approvers, dispute desk, rules, insights, admin
│   └── customer/                   <- Bengali-first complaint portal (PWA)
├── packages/
│   ├── ui/                         <- design system
│   ├── api-client/                 <- generated from OpenAPI
│   ├── api-contract/openapi.yaml
│   ├── i18n/                       <- bn and en catalogs
│   ├── mocks/                      <- MSW handlers
│   └── config/
├── deploy/
│   ├── docker-compose.yml
│   ├── k8s/                        <- manifests or Kustomize overlays
│   └── observability/              <- Prometheus, Grafana JSON, alert rules
├── load-tests/                     <- k6 scripts
├── .github/workflows/
└── Makefile
```

Single-repo setup: put the five files as shown (`docs/SRS.md`, `docs/RESEARCH.md`, `docs/prompts/*`, root `README.md`). The prompts tell the agent to read `docs/SRS.md` and to use `CLAUDE.md` at the repo root, so no path edits are needed.

## Workflow

1. Create two folders or one monorepo: `backend/` and `frontend/` (or `apps/` and `services/`). Copy `SRS.md` to `docs/SRS.md` in each.
2. Save each shared brief as `CLAUDE.md` in the matching repo root. In Antigravity, add it as the project rules file.
3. Run phases in order. Backend and frontend can run in parallel after B0 and F0 because the frontend uses mocks generated from the OpenAPI file.
4. After each phase: run the tests, read the diff, commit. Do not stack phases without reviewing.
5. Keep a `docs/adr/` folder. Every decision you can explain is an interview answer.

Suggested order of work: B0 and F0, then B1 to B3 with F1 (mocks), B4 and B5 with F1, B6 with F2 and F3, B7 with F4, B8, B9 and F5.

## Tips for working with the agent

- Give one phase per session. Start by asking it to restate the requirement IDs it will implement.
- Ask it to write the tests first for money, idempotency, SLA and four-eyes logic.
- If output looks plausible but you cannot explain it, ask for a walkthrough and an ADR before moving on.
- Pin library versions after the first successful build.

## Making it help you get hired

- Lead the README with the problem, a short demo GIF and the evaluation table (precision, recall, alert reduction, SLA compliance), including honest limitations.
- Commit `docs/performance.md`, `docs/evaluation.md`, the chaos test results and the threat model.
- Prepare to explain: why alert grouping, how the graph finds rings, why four-eyes, how idempotency and the outbox prevent duplicate refunds, how working-day SLA math works, and what you would change at 100 times the scale.
- Put the live demo link, the video and a short write-up on your profile.
- CV line example (use your real numbers): "Built a fraud and dispute platform scoring events at p95 under N ms at N events per second, with explainable rules, graph-based mule-ring detection and SLA-driven dispute workflows."

## Honesty

Astha is a portfolio system. It uses synthetic data only, moves no real money, is not affiliated with any provider or regulator, and is not a licensed product. Regulatory durations are configurable defaults taken from news reports of a Bangladesh Bank circular; confirm against the original circular before relying on them.
