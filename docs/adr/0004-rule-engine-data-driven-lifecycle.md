# ADR 0004: Data-Driven Rule Catalog, Four-Eyes Approval, and Versioned Lifecycle

## Status
Accepted

## Context
Requirements RSK-1, RSK-3, RSK-4, RSK-5, and RSK-7 specify that fraud rules R01 through R10 must be data-driven, configurable in the database, versioned, auditable, and hot-reloadable without redeployment. Furthermore, changes to rules or thresholds must enforce a four-eyes governance model where the rule author cannot approve their own rule changes.

## Decision
1. **Rule Representation**:
   - Each rule definition consists of a unique rule code (e.g., `R01_PASS_THROUGH_MULE`), typology, severity (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), weight (0-100), and parameter payload stored as JSON in `rules` and `rule_versions` tables.
   - Parameters include thresholds such as `maxAccountAgeDays`, `minInflowPaisa`, `minOutflowRatio`, `windowSeconds`, etc.
2. **Version Lifecycle**:
   - Status transitions: `DRAFT` -> `PENDING_APPROVAL` -> `ACTIVE` | `REJECTED` | `ARCHIVED`.
   - Editing a rule creates a new incremented version in `DRAFT` status.
   - Any modification requires approval by a distinct user with role `RISK_MANAGER`. Attempting self-approval returns HTTP 403 / 422 with an RFC 9457 error.
3. **Cache & Invalidation**:
   - Active rules are cached in-memory with a version fingerprint.
   - When a rule version is activated or rolled back, a cache invalidation event refreshes the engine's active catalog in real time without service restarts.
4. **Shadow Mode & Backtesting**:
   - Candidates can be executed in `SHADOW` mode, recording results in `shadow_results` without altering customer decisions.
   - Backtesting re-runs candidate rules against stored feature snapshots and evaluates precision, recall, and alert volume against ground-truth labels.

## Consequences
- No code redeployment needed to adjust risk thresholds.
- Enforced four-eyes regulatory compliance on all parameter changes.
- Safe rollback to any prior active version in a single API call.
