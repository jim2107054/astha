# ADR 0001: Monorepo Architecture and Service Boundaries

## Status
Accepted

## Context
Astha is a real-time fraud and dispute operations platform for Bangladeshi mobile financial services. The system requires high-throughput stream ingestion, low-latency scoring (<100ms p95), graph processing, dispute state machines, and rich operations consoles for analysts and customers.
We need clear service boundaries with independent domain persistence while maintaining a single, cohesive repository for shared event contracts, tooling, and synchronized deployments.

## Decision
1. **Repository Topology**: Adopt a unified monorepo:
   - `services/` contains Gradle multi-module Spring Boot 3 services on Java 21.
   - `apps/` contains Next.js web applications (`apps/console` and `apps/customer`).
   - `packages/` contains shared frontend packages (`ui`, `api-client`, `api-contract`, `i18n`, `mocks`, `config`) managed via pnpm and Turborepo.
   - `contracts/` contains JSON Schemas for inter-service Kafka topics.
2. **Service Boundaries**:
   - `platform-lib`: Common cross-cutting concerns (RFC 9457 problem details, idempotency filter, transactional outbox, audit helpers, security/RBAC annotations).
   - `risk-service`: Real-time Redis-backed feature extraction, dynamic rule evaluation (R01-R10), decision scoring, rule lifecycle management.
   - `case-service`: Alert deduplication, grouping into entity/ring cases, priority triage, four-eyes authorization workflow.
   - `dispute-service`: Regulatory dispute state machine, working-day SLA clocks (excluding Friday & Saturday), auto-refund coordinator.
   - `graph-service`: Entity-link graph builder and mule ring detection.
   - `simulator-service` & `provider-mock`: Deterministic synthetic population, typologies, and mock MFS banking rails.

## Consequences
- Fast cross-service contract verification in CI.
- One repository to clone and build with standard toolchains.
- Independent database schemas per service ensure domain isolation and zero hidden cross-domain table joins.
