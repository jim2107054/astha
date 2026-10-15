# Astha Requirements Traceability Matrix

This document tracks implementation and test coverage for all requirements defined in `docs/SRS.md`. Commits and tests cite these requirement IDs.

| Req ID | Description | Component | Implementation Files | Test Names & Suite | Status |
| --- | --- | --- | --- | --- | --- |
| **RSK-1** | Compute risk score 0 to 100 with reason codes and replayable snapshot | `risk-service` | `com.astha.risk.engine.RiskEngine`, `ScoreResponse` | `RiskServiceIntegrationTest#shouldScoreTransactionViaApi`, `FeatureSnapshotReplayTest#shouldProduceDeterministicScoreOnReplay` | **VERIFIED** |
| **RSK-2** | Real-time features in Redis (rolling counts, sums, ratios) | `risk-service` | `com.astha.risk.features.RedisFeatureStore`, `FeatureSnapshot` | `RuleCatalogTest#shouldHitR01PassThroughMule`, `DegradedModeTest` | **VERIFIED** |
| **RSK-3** | Data-driven rule catalog (R01 to R10) | `risk-service` | `com.astha.risk.rules.R01PassThroughMuleRule` through `R10FailedDebitWatchRule` | `RuleCatalogTest` (all 10 rules R01-R10 tested with boundary values) | **VERIFIED** |
| **RSK-4** | Rule changes versioned, require risk manager approval, no redeploy | `risk-service`, `apps/console` | `com.astha.risk.service.RuleManagementService`, `apps/console/src/app/page.tsx` | `FourEyesApprovalTest#shouldEnforceFourEyesPolicy`, `RiskServiceIntegrationTest#shouldListRules` | **VERIFIED** |
| **RSK-5** | Shadow mode: candidate rules run without affecting decisions | `risk-service`, `apps/console` | `com.astha.risk.domain.ShadowResult`, `RiskEngine#evaluateShadowRules` | `RiskEngine` shadow results evaluation & persistence tests | **VERIFIED** |
| **RSK-6** | Decision API (`POST /v1/risk/score`) with 100ms budget & degraded fallback | `risk-service` | `com.astha.risk.controller.RiskScoreController`, `RiskEngine` | `DegradedModeTest#shouldFallbackGracefullyWhenFeatureStoreIsDown`, `RiskServiceIntegrationTest#shouldMeetP95LatencyBudget` | **VERIFIED** |
| **RSK-7** | Back-test a rule version against stored history | `risk-service`, `apps/console` | `RuleManagementService#runBacktest`, `RuleController#runBacktest` | `RuleControllerTest`, backtest runner in Console UI | **VERIFIED** |
| **RSK-8** | Allowlist and blocklist management with expiry & audit | `risk-service`, `apps/console` | `com.astha.risk.domain.ListEntity`, `com.astha.risk.controller.ListController` | `ListsManagementTest#shouldBlockListedAccount`, `ListsManagementTest#shouldAllowListedAccount` | **VERIFIED** |
| **R01** | Pass-through mule rule | `risk-service` | `com.astha.risk.rules.R01PassThroughMuleRule` | `RuleCatalogTest#shouldHitR01PassThroughMule` | **VERIFIED** |
| **R02** | Young account burst rule | `risk-service` | `com.astha.risk.rules.R02YoungAccountBurstRule` | `RuleCatalogTest#shouldHitR02YoungAccountBurst` | **VERIFIED** |
| **R03** | Bangla QR cash-substitute merchant rule | `risk-service` | `com.astha.risk.rules.R03QrCashSubstituteRule` | `RuleCatalogTest#shouldHitR03QrCashSubstitute` | **VERIFIED** |
| **R04** | Account takeover rule | `risk-service` | `com.astha.risk.rules.R04AccountTakeoverRule` | `RuleCatalogTest#shouldHitR04AccountTakeover` | **VERIFIED** |
| **R05** | Structuring near-limit rule | `risk-service` | `com.astha.risk.rules.R05StructuringRule` | `RuleCatalogTest#shouldHitR05Structuring` | **VERIFIED** |
| **R06** | Agent anomaly velocity rule | `risk-service` | `com.astha.risk.rules.R06AgentAnomalyRule` | `RuleCatalogTest#shouldHitR06AgentAnomaly` | **VERIFIED** |
| **R07** | Dormant account wake-up rule | `risk-service` | `com.astha.risk.rules.R07DormantWakeUpRule` | `RuleCatalogTest#shouldHitR07DormantWakeUp` | **VERIFIED** |
| **R08** | Shared device or listed entity rule | `risk-service` | `com.astha.risk.rules.R08SharedDeviceListedRule` | `RuleCatalogTest#shouldHitR08SharedDeviceListed` | **VERIFIED** |
| **R09** | Graph mule ring score rule | `risk-service` | `com.astha.risk.rules.R09RingScoreRule` | `RuleCatalogTest#shouldHitR09RingScore` | **VERIFIED** |
| **R10** | Failed-debit watch rule | `risk-service` | `com.astha.risk.rules.R10FailedDebitWatchRule` | `RuleCatalogTest#shouldHitR10FailedDebitWatch` | **VERIFIED** |
| **CAS-1..10** | Fraud analyst queue, triage score breakdown, PII masking, four-eyes freeze | `apps/console`, `packages/ui` | `apps/console/src/app/page.tsx`, `packages/ui/src/components/*` | `Console.test.ts`, Playwright `analyst-workflow.spec.ts` | **VERIFIED** |
| **GRP-4** | Graph query API & interactive topology explorer | `apps/console`, `packages/ui` | `packages/ui/src/components/GraphCanvas.tsx` | Playwright `analyst-workflow.spec.ts` | **VERIFIED** |
| **NFR-1** | Decision latency p95 under 100 ms at 1,000 eps | `risk-service` | `com.astha.risk.engine.RiskEngine` | `RiskServiceIntegrationTest#shouldMeetP95LatencyBudget` | **VERIFIED** |
| **NFR-5** | Safe degradation to `ALLOW_WITH_FLAG` on Redis timeout | `risk-service` | `com.astha.risk.engine.RiskEngine` | `DegradedModeTest#shouldFallbackGracefullyWhenFeatureStoreIsDown` | **VERIFIED** |
| **NFR-6** | 100% of decisions have reason codes and replayable snapshot | `risk-service` | `com.astha.risk.domain.Decision` | `FeatureSnapshotReplayTest#shouldProduceDeterministicScoreOnReplay` | **VERIFIED** |
| **NFR-11** | Quality gates, unit test coverage, and Playwright verification | `all` | Full Gradle check & Vitest test suites | CI pipeline verification | **VERIFIED** |
| **NFR-13** | Localization in Bengali and English with taka format | `packages/i18n` | `formatMoney.ts`, `formatDate.ts`, `locales/*.json` | `formatMoney.test.ts`, `formatDate.test.ts` | **VERIFIED** |
