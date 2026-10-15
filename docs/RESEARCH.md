# Astha Research: The Problem Worth Solving

Prepared 15 Oct 2026. Source pages were read as search results, not full-page fetches, so verify numbers before quoting them in a CV, README or interview. Several statistics come from vendors or news summaries and are marked.

## 1. Question

What problems do most fintechs face, and which one can one engineer solve with a working, demonstrable system that also helps in a fintech hiring process (Bangladesh first, global second)?

## 2. What the research found

### 2.1 Global problems (ranked by how often they appear)

| # | Problem | Evidence | Source |
| --- | --- | --- | --- |
| 1 | Fraud, now AI-assisted | In Alloy's 2026 State of Fraud survey, 91% of 500+ leaders saw more AI-driven financial crime and 94% rank fraud risk first when planning products (reported secondhand) | [NetSuite](https://www.netsuite.com/portal/resource/articles/financial-management/fintech-challenges.shtml) |
| 2 | AML and KYC compliance burden, with alert fatigue | Rule-based monitoring is noisy; one commentary says 90 to 99% of alerts turn out false (vendor and blog estimates, not audited) and another puts investigation cost at roughly $500 to $1,500 per alert | [HackerNoon](https://hackernoon.com/why-aml-transaction-monitoring-fails-when-you-optimise-for-volume-instead-of-decisions), [Flagright](https://www.flagright.com/post/understanding-false-positives-in-transaction-monitoring) |
| 3 | Manual reconciliation and payment operations | Modern Treasury's 2025 report (via DeUna): 98% of companies still do some payment operations manually; PwC figure quoted: about 30% of finance time goes to manual reconciliation | [DeUna](https://www.deuna.com/post/reconciliation-the-most-expensive-process), [Spark](https://www.spark.money/glossary/reconciliation) |
| 4 | Regulatory fragmentation and fines | N26 was fined $10 million for late suspicious-activity reporting (reported by an agency blog) | [Uptech](https://www.uptech.team/blog/fintech-challenges) |
| 5 | Outages and reliability | Vendor surveys put payment downtime in the millions per year per market; treat as directional | [Dynatrace/FreedomPay summary](https://wetheflywheel.com/en/guides/cost-of-downtime-2026/) |
| 6 | Dependency on middleware or BaaS partners | Synapse's collapse left a reported $85 million gap between bank holdings and customer balances | [SolveXia](https://solvexia.com/blog/payment-reconciliation-blueprint) |

### 2.2 Bangladesh-specific problems

| # | Problem | Evidence | Source |
| --- | --- | --- | --- |
| 1 | Mobile-money fraud at scale | A study cited in May 2026 found 6.3% of individual MFS users and 17% of agents had been victims; analysts estimated about Tk 21,000 crore lost over the years; many accounts are fake, borrowed or weakly verified | [Observer BD](https://www.observerbd.com/news/575239) |
| 2 | Mule accounts and fast cash-out | Complaint-based research found scam payments typically start in MFS then move to banks; red flags were multiple unrelated deposits, rapid cash-out, weak KYC and short-lived accounts | [ScienceDirect](https://www.sciencedirect.com/science/article/pii/S2949791426000461) |
| 3 | Bangla QR abused as a cash-out channel | Reports describe merchants handing over cash (for example Tk 9,815 for a Tk 10,000 QR payment) to avoid normal cash-out charges of Tk 13 to Tk 18.50 per Tk 1,000 and the daily cash-out limit; this hurts provider economics and data quality | [FE](https://today.thefinancialexpress.com.bd/last-page/bangla-qr-misuse-raises-concerns-over-mfs-sustainability-1789410537), [TBS](https://www.tbsnews.net/bangladesh/how-abuse-bangla-qr-threatens-countrys-mfs-ecosystem-1538591) |
| 4 | Slow, unresolved disputes | Reporting in March 2026 describes complaints (failed transactions, mistaken transfers, delayed refunds, merchant disputes) that stay unresolved or settle late; a TIB study is cited; an earlier IPA survey found most users who complained were not satisfied | [Observer BD](https://observerbd.com/news/570386), [IPA](https://poverty-action.org/bangladesh-consumer-protection-digital-finance-survey) |
| 5 | New dispute rules arriving | Bangladesh Bank issued Bangla QR dispute guidelines on 27 Sep 2026, effective 1 Dec 2026 (see 3.2) | [Dhaka Tribune](https://www.dhakatribune.com/business/banks/420879/bb-issues-guidelines-for-dispute-resolution-in) |
| 6 | Heavy cash-out dependence and low digital literacy | About 1.85 million agents; many users rely on agents and are exposed to OTP phishing and fake customer-care calls | [Financial Express](https://thefinancialexpress.com.bd/special-issues/bangladesh-on-cusp-of-transition-to-cashless-economy) |
| 7 | e-KYC depends on one national ID API | A July 2026 survey of 61 banks and 35 NBFIs reports mostly manual verification and calls the Election Commission API a single point of failure | [Delve summary](https://delvetool.com/research/pervez-2026-effectiveness-of-national-id-as-a-unique-identifier-for-customer-onboarding-in-financial-services-in-bangladesh-a-survey) |
| 8 | Customer money stuck after platform collapse | 58,000+ e-commerce complaints, Tk 3,869 crore claimed, about Tk 531 crore trapped in gateways, Tk 472 crore refunded as of Aug 2026 | [Dhaka Tribune](https://www.dhakatribune.com/business/418715/over-58-000-e-commerce-complaints-lodged) |

### 2.3 Hiring signal

Postings seen earlier name Kafka, Postgres, event-driven design, ledgers, reconciliation, PCI-DSS, AML/KYC and fraud detection as wanted skills (see the earlier TakaFlow document for links). A fraud and dispute platform exercises almost all of them.

## 3. Choosing the problem

### 3.1 Scoring

Scores are 1 to 5, my judgment, not data.

| Candidate problem | Severity in Bangladesh | Fits a solo build | Hiring signal | Demonstrable with synthetic data | Total |
| --- | --- | --- | --- | --- | --- |
| A. Fraud and mule-account detection plus alert triage | 5 | 4 | 5 | 5 | 19 |
| B. Slow, opaque dispute and complaint handling | 5 | 5 | 4 | 5 | 19 |
| C. Manual reconciliation | 3 | 5 | 4 | 5 | 17 |
| D. e-KYC fragmentation | 4 | 2 (needs government API) | 3 | 2 | 11 |
| E. Outage resilience | 3 | 3 | 4 | 3 | 13 |

A and B tie, and they share data, events and users: a flagged fraud case usually becomes a dispute, and a dispute is evidence for fraud rules. Combining them gives one coherent product.

### 3.2 Why the timing helps

Bangladesh Bank's Bangla QR dispute guidelines take effect on 1 Dec 2026. As reported: customers need not file a separate complaint for confirmed failed transactions (automatic refund); same-provider disputes must be resolved within three working days; cross-provider disputes run through NPSB's central Dispute Management System; customers can complain within 45 days; the full process, including chargeback and arbitration, must finish within 30 days; fraud and tampering investigations also have a 30-day limit ([Dhaka Tribune](https://www.dhakatribune.com/business/banks/420879/bb-issues-guidelines-for-dispute-resolution-in)). Providers need tooling that tracks these clocks. The circular text itself was not read; confirm details against the original before relying on them.

## 4. Decision

**Problem statement.** Bangladeshi mobile-money and payment providers lose money and trust because fraud (mule accounts, OTP account takeover, Bangla QR cash-out abuse) is detected late and noisily, and customer disputes are resolved slowly without clear deadlines.

**Solution: Astha**, a real-time fraud and dispute operations platform that:

1. Scores every transaction event in under 100 ms with explainable reason codes.
2. Finds mule rings and cash-out abuse patterns using graph and behavior features.
3. Turns noisy alerts into prioritized, deduplicated cases with an analyst feedback loop that measures and reduces false positives.
4. Runs disputes on SLA clocks aligned with the reported Bangladesh Bank guideline, including automatic refund of confirmed failed transactions.
5. Gives customers a Bengali status tracker so they know where their complaint stands.

Everything runs on synthetic data from a built-in simulator. Astha is a portfolio system, not a licensed product, and uses no real customer data.

## 5. Limits of this research

- Several figures come from news summaries, vendors or blogs, not primary datasets.
- Market share and fraud-loss estimates vary between sources.
- The Bangladesh Bank circular was reported, not read; no BFIU rule text was read.
- Nothing here is legal or regulatory advice.
