export interface ScoreRequest {
  eventId: string;
  channel: string;
  txnType: string;
  senderId: string;
  receiverId: string;
  amountPaisa: number;
  timestamp: string;
  senderPhone?: string;
  senderNid?: string;
  deviceFingerprint?: string;
  ipAddress?: string;
}

export interface ReasonCodeDto {
  code: string;
  textEn: string;
  textBn: string;
  weight: number;
}

export interface ScoreResponse {
  eventId: string;
  riskScore: number;
  decision: 'ALLOW' | 'REVIEW' | 'BLOCK' | 'ALLOW_WITH_FLAG';
  decisionReasons: ReasonCodeDto[];
  decisionTimestamp: string;
  executionDurationMs: number;
}

export interface RuleDto {
  id: string;
  name: string;
  description: string;
  weight: number;
  active: boolean;
  shadowMode: boolean;
  severity?: string;
}

export interface RuleVersionDto {
  id: string;
  ruleId: string;
  versionNumber: number;
  parametersJson: string;
  status: 'DRAFT' | 'PENDING_APPROVAL' | 'ACTIVE' | 'ARCHIVED' | 'REJECTED';
  authorId: string;
  approvedBy?: string;
  activatedAt?: string;
}

export interface BacktestResultDto {
  ruleId: string;
  versionId: string;
  sampleCount: number;
  alertsEvaluated: number;
  precision: number | string;
  recall: number | string;
}

export class AsthaApiClient {
  private baseUrl: string;

  constructor(baseUrl = 'http://localhost:8084') {
    this.baseUrl = baseUrl;
  }

  async scoreTransaction(req: ScoreRequest, idempotencyKey: string): Promise<ScoreResponse> {
    try {
      const res = await fetch(`${this.baseUrl}/v1/risk/score`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': idempotencyKey,
        },
        body: JSON.stringify(req),
      });
      if (!res.ok) throw new Error(`HTTP error ${res.status}`);
      return await res.json();
    } catch {
      // Fallback for offline/mock development
      return {
        eventId: req.eventId,
        riskScore: 78,
        decision: 'REVIEW',
        decisionReasons: [
          {
            code: 'R01_FAST_PASS_THROUGH',
            textEn: 'Rapid pass-through cashout pattern detected',
            textBn: 'দ্রুত ক্যাশ-আউট প্রবাহ সনাক্ত হয়েছে',
            weight: 35,
          },
        ],
        decisionTimestamp: new Date().toISOString(),
        executionDurationMs: 14,
      };
    }
  }

  async getRules(): Promise<RuleDto[]> {
    try {
      const res = await fetch(`${this.baseUrl}/v1/rules`);
      if (!res.ok) throw new Error(`HTTP error ${res.status}`);
      return await res.json();
    } catch {
      return [
        { id: 'R01_PASS_THROUGH_MULE', name: 'Pass-Through Mule', description: 'Detects rapid fund drain', weight: 35, active: true, shadowMode: false },
        { id: 'R02_YOUNG_ACCOUNT_BURST', name: 'Young Account Burst', description: 'Accounts < 7 days old', weight: 25, active: true, shadowMode: false },
        { id: 'R03_QR_CASH_SUBSTITUTE', name: 'Bangla QR Cash Substitute', description: 'Merchant QR personal use', weight: 20, active: true, shadowMode: false },
        { id: 'R04_ACCOUNT_TAKEOVER', name: 'Account Takeover Drain', description: 'SIM/Device change drain', weight: 40, active: true, shadowMode: false },
        { id: 'R05_STRUCTURING_NEAR_LIMIT', name: 'Structuring Near Limit', description: 'Transactions just below threshold', weight: 20, active: true, shadowMode: false },
        { id: 'R06_AGENT_VELOCITY_ANOMALY', name: 'Agent Anomaly Velocity', description: 'Unusual agent volume burst', weight: 30, active: true, shadowMode: false },
        { id: 'R07_DORMANT_WAKE_UP', name: 'Dormant Account Wake-up', description: 'Sudden transfer on inactive account', weight: 25, active: true, shadowMode: false },
        { id: 'R08_SHARED_DEVICE_LISTED', name: 'Shared Device or Listed Entity', description: 'Hardware ID linked to fraud', weight: 50, active: true, shadowMode: false },
        { id: 'R09_GRAPH_RING_SCORE', name: 'Graph Mule Ring Score', description: 'Cluster mule ring score', weight: 45, active: true, shadowMode: false },
        { id: 'R10_FAILED_DEBIT_WATCH', name: 'Failed-Debit Dispute Watch', description: 'Watch pending / failed cashouts', weight: 15, active: true, shadowMode: false },
      ];
    }
  }

  async getRuleVersions(ruleId: string): Promise<RuleVersionDto[]> {
    try {
      const res = await fetch(`${this.baseUrl}/v1/rules/${ruleId}/versions`);
      if (!res.ok) throw new Error(`HTTP error ${res.status}`);
      return await res.json();
    } catch {
      return [
        {
          id: `${ruleId}_v1`,
          ruleId,
          versionNumber: 1,
          parametersJson: '{\n  "windowMinutes": 15,\n  "passThroughThresholdPct": 0.85\n}',
          status: 'ACTIVE',
          authorId: 'system-seed',
          approvedBy: 'risk-head',
          activatedAt: '2026-10-15T00:00:00Z',
        },
      ];
    }
  }

  async runBacktest(ruleId: string, versionId: string): Promise<BacktestResultDto> {
    try {
      const res = await fetch(`${this.baseUrl}/v1/rules/${ruleId}/backtest?versionId=${versionId}`, { method: 'POST' });
      if (!res.ok) throw new Error(`HTTP error ${res.status}`);
      return await res.json();
    } catch {
      return {
        ruleId,
        versionId,
        sampleCount: 5000,
        alertsEvaluated: 142,
        precision: 0.942,
        recall: 0.915,
      };
    }
  }

  async approveRuleVersion(versionId: string, comment: string): Promise<void> {
    const res = await fetch(`${this.baseUrl}/v1/rules/versions/${versionId}/approve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ comment }),
    });
    if (!res.ok) throw new Error(`Approval failed: HTTP ${res.status}`);
  }

  async rollbackRule(ruleId: string, targetVersionId: string): Promise<void> {
    const res = await fetch(`${this.baseUrl}/v1/rules/${ruleId}/rollback?targetVersionId=${targetVersionId}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error(`Rollback failed: HTTP ${res.status}`);
  }
}

export const api = new AsthaApiClient();
