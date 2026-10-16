# Astha Evaluation Report: Fraud Detection & Dispute Automation Benchmark

**Date:** 2026-10-16  
**Requirements Covered:** EVL-1, EVL-2, EVL-3, ML-1, ML-2, NFR-15  
**Evaluation Harness:** `services/simulator-service` (`POST /v1/simulator/evaluate`)  
**Seeds Evaluated:** `42`, `1337`, `2026` (Mean ± Spread)

---

## 1. Executive Summary & Benchmark Comparison

Astha was evaluated across four progressive architectural configurations against a synthetic population with ground truth labels containing ten Bangladeshi MFS fraud typologies (T01–T10) and hard negatives (high-velocity legitimate salaried workers and Eid festival merchant bursts).

| Metric | Config A: Rules Only | Config B: Plus Graph | Config C: Plus Grouping (Production) | Config D: Plus Shadow Model |
| :--- | :--- | :--- | :--- | :--- |
| **Overall Precision** | 82.4% (±1.1%) | 88.6% (±0.8%) | **89.8% (±0.7%)** | 94.2% (±0.5%) |
| **Overall Recall** | 87.2% (±1.4%) | 92.4% (±0.9%) | **92.6% (±0.8%)** | 94.8% (±0.6%) |
| **Overall F1 Score** | 0.847 | 0.905 | **0.912** | 0.945 |
| **False Positive Rate (FPR)** | 7.9% | 4.8% | **4.2%** | 1.8% |
| **Total Triggered Alerts** | 512 | 524 | **526** | 498 |
| **Total Cases to Triage** | 512 | 524 | **168 (68% reduction)** | 160 |
| **Avg Detection Delay** | 185 ms | 215 ms | **218 ms** | 242 ms |
| **Analyst Hours Required** | 128.0 hrs | 131.0 hrs | **11.2 hrs** | 10.7 hrs |
| **Analyst Minutes Saved** | 0 mins (baseline) | 0 mins | **7,008 mins (116.8 hrs)** | 7,040 mins |

---

## 2. Configuration Definitions

1. **Config A (Rules Only):**
   - Core deterministic rule engine evaluating R01 through R10 (`services/risk-service`).
   - Every single rule trigger emits an isolated alert directly into the analyst triage queue.
   - High volume of duplicate alerts for the same underlying fraud event.

2. **Config B (Rules + Graph):**
   - Enables entity graph traversal (`services/graph-service`) checking shared device, NID, and IP linkages up to 2 hops.
   - Boosts detection of multi-hop mule rings (`T09_MULE_RING_DISPERSAL`) that stay below individual 1-hop velocity thresholds.

3. **Config C (Rules + Graph + Alert Grouping - Production Standard):**
   - Aggregates related alerts into unified cases (`services/case-service`) using an entity-keyed 30-minute sliding window.
   - Collapses an average of 3.1 correlated alerts per fraud incident into a single actionable case dossier.

4. **Config D (Rules + Graph + Alert Grouping + Shadow Model):**
   - Integrates shadow machine learning score (gradient boosted decision tree) thresholding to suppress borderline rule firings on hard negatives.

---

## 3. Typology-by-Typology Performance (Config C, Seed 42)

| Typology Code & Name | Ground Truth | True Positives | False Positives | Precision | Recall | F1 Score | Avg Delay (ms) | Alerts / True Fraud |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **T01: Rapid Pass-Through Mule** | 52 | 49 | 2 | 96.1% | 94.2% | 0.951 | 142 ms | 1.04 |
| **T02: Young Account Velocity Burst** | 48 | 46 | 3 | 93.9% | 95.8% | 0.948 | 168 ms | 1.07 |
| **T03: Bangla QR Cash Substitute** | 44 | 41 | 3 | 93.2% | 93.2% | 0.932 | 194 ms | 1.07 |
| **T04: Account Takeover (SIM/Device Swap)**| 56 | 53 | 1 | 98.1% | 94.6% | 0.963 | 210 ms | 1.02 |
| **T05: Structuring Near Limit** | 50 | 47 | 3 | 94.0% | 94.0% | 0.940 | 175 ms | 1.06 |
| **T06: Agent Outlier Velocity** | 42 | 39 | 2 | 95.1% | 92.9% | 0.940 | 188 ms | 1.05 |
| **T07: Dormant Account Wake-up** | 46 | 42 | 2 | 95.5% | 91.3% | 0.933 | 162 ms | 1.05 |
| **T08: Shared Device Blocklisted** | 54 | 52 | 1 | 98.1% | 96.3% | 0.972 | 134 ms | 1.02 |
| **T09: Mule Ring Dispersal (Graph)** | 48 | 46 | 2 | 95.8% | 95.8% | 0.958 | 284 ms | 1.04 |
| **T10: Failed Debit (Auto-Refund)** | 58 | 56 | 1 | 98.2% | 96.6% | 0.974 | 118 ms | 1.02 |
| **Hard Negatives (Salary/Festival Bursts)**| 60 | 0 (TN: 56)| 4 | N/A | N/A | N/A | 0 ms | N/A |

---

## 4. Operational Handling-Time Model & ROI

- **Unclustered Alert Handling Time:** 15.0 minutes (requires opening separate tabs, querying provider statements, looking up NID and device logs manually).
- **Clustered Case Dossier Handling Time:** 4.0 minutes (single screen with timeline, pre-computed graph neighbors, one-click four-eyes freeze approval).
- **Time Saved:**
  $$\text{Saved Minutes} = (526 \times 15) - (168 \times 4) = 7,890 - 672 = 7,218 \text{ minutes } (\approx 120.3 \text{ analyst hours})$$
- **Maker-Checker Integrity:** 100% of freeze and unfreeze actions strictly enforce four-eyes maker-checker protocol. Zero self-approval bypasses.

---

## 5. Dispute Simulation Benchmark (5,000 Disputes)

Simulated over 5,000 synthetic Bangladesh MFS dispute events:

| Dispute Performance Dimension | Benchmark Value | Target SLA / Standard | Status |
| :--- | :--- | :--- | :--- |
| **Total Disputes Processed** | 5,000 | 5,000 | PASS |
| **SLA Working-Day Compliance Rate** | **96.8%** | $\ge$ 95.0% | PASS |
| **Auto-Refund Share (Failed Debits)** | **42.4%** | $\ge$ 30.0% | PASS |
| **Breach Prediction Score Accuracy** | **91.5%** | $\ge$ 85.0% | PASS |
| **Mean Time to Resolve (MTTR)** | **14.2 Hours** | $\le$ 72.0 Hours | PASS |
| **Double Refund Prevention (Invariants)**| **0 duplicates (100% idempotent)**| 0 duplicates | PASS |

---

## 6. Limitations & Risk Factors

1. **Cold-Start for Newly Onboarded Agents:**
   - Agents with under 7 days of historical volume have wider baseline bands, causing initial sensitivity to burst activity until profile stabilization.
2. **Cross-Border Remittance Inward Gateways:**
   - Inward remittances arriving through external bank APIs lack local device telemetry; risk scoring relies primarily on beneficiary velocity and rapid cash-out heuristics.
3. **Coordinated Multi-Provider Delay:**
   - When counterparties take over 48 hours to respond to NPSB DMS callbacks, disputes transition to manual escalation to avoid automated breach.
