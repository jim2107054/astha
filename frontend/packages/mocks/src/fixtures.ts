import { ReasonCodeItem, GraphNode, GraphEdge } from '@astha/ui';

export interface MockCase {
  id: string;
  entityId: string;
  entityType: 'ACCOUNT' | 'MERCHANT' | 'AGENT';
  status: 'NEW' | 'INVESTIGATING' | 'PENDING_APPROVAL' | 'CLOSED';
  priority: number;
  typology: string;
  maxRiskScore: number;
  amountAtRiskPaisa: number;
  alertCount: number;
  createdAt: string;
  assignee: string;
  phone: string;
  nid: string;
  reasonCodes: ReasonCodeItem[];
  timeline: Array<{ time: string; event: string }>;
  comments: Array<{ time: string; user: string; text: string }>;
}

export const mockCases: MockCase[] = [
  {
    id: 'CAS-2026-001',
    entityId: 'acc-01711223344',
    entityType: 'ACCOUNT',
    status: 'NEW',
    priority: 95,
    typology: 'R01_PASS_THROUGH_MULE',
    maxRiskScore: 92,
    amountAtRiskPaisa: 28500000, // 2,85,000 Taka
    alertCount: 4,
    createdAt: '2026-10-15T08:30:00Z',
    assignee: 'Unassigned',
    phone: '+8801711223344',
    nid: '1995267182910',
    reasonCodes: [
      {
        code: 'R01_FAST_PASS_THROUGH',
        textEn: 'Rapid pass-through mule pattern: 8 unrelated senders followed by immediate cash-out drain',
        textBn: 'দ্রুত পাস-থ্রু মিউল প্যাটার্ন: ৮ জন অপরিচিত প্রেরক থেকে অর্থ প্রাপ্তির পর দ্রুত ক্যাশ-আউট',
        weight: 35,
      },
      {
        code: 'R02_YOUNG_ACCOUNT_BURST',
        textEn: 'Account age 4 days received high velocity inflow exceeding tier limit',
        textBn: '৪ দিনের নতুন অ্যাকাউন্টে নির্ধারিত সীমার অতিরিক্ত লেনদেন প্রবাহ',
        weight: 25,
      },
      {
        code: 'R08_SHARED_DEVICE_LISTED',
        textEn: 'Device IMEI shared with 5 known blocked accounts in Dhaka cluster',
        textBn: 'ডিভাইস আইএমইআই (IMEI) চিহ্নিত ৫টি ব্লকড অ্যাকাউন্টের সাথে সম্পৃক্ত',
        weight: 32,
      },
    ],
    timeline: [
      { time: '2026-10-15T08:15:10Z', event: 'First velocity burst alert triggered (R02)' },
      { time: '2026-10-15T08:22:45Z', event: 'Cash-out drain at Agent acc-agent-mirpur-04 (R01)' },
      { time: '2026-10-15T08:30:00Z', event: 'Case synthesized & escalated to P95 priority queue' },
    ],
    comments: [
      {
        time: '2026-10-15T08:35:00Z',
        user: 'system-agent',
        text: 'Automated cluster expansion linked this MSISDN to ring #CR-8821.',
      },
    ],
  },
  {
    id: 'CAS-2026-002',
    entityId: 'acc-01822334455',
    entityType: 'ACCOUNT',
    status: 'INVESTIGATING',
    priority: 88,
    typology: 'R04_ACCOUNT_TAKEOVER',
    maxRiskScore: 84,
    amountAtRiskPaisa: 15000000, // 1,50,000 Taka
    alertCount: 2,
    createdAt: '2026-10-15T09:10:00Z',
    assignee: 'analyst-nafis',
    phone: '+8801822334455',
    nid: '1988349281723',
    reasonCodes: [
      {
        code: 'R04_ACCOUNT_TAKEOVER',
        textEn: 'SIM swap detected 2.5 hours before full balance liquidation via Send Money',
        textBn: 'ব্যালেন্স স্থানান্তরের ২.৫ ঘণ্টা পূর্বে সিম সোয়াপ সনাক্ত হয়েছে',
        weight: 40,
      },
      {
        code: 'R07_DORMANT_WAKE_UP',
        textEn: 'Account dormant for 180 days suddenly transferred maximum daily limit',
        textBn: '১৮০ দিন নিষ্ক্রিয় থাকা অ্যাকাউন্টে হঠাৎ সর্বোচ্চ দৈনিক সীমার লেনদেন',
        weight: 25,
      },
    ],
    timeline: [
      { time: '2026-10-15T07:00:00Z', event: 'SIM replacement notification received from telco API' },
      { time: '2026-10-15T09:05:00Z', event: 'PIN reset and password change from new device' },
      { time: '2026-10-15T09:10:00Z', event: 'Outgoing transfer 1,50,000 BDT to acc-01999888777' },
    ],
    comments: [
      {
        time: '2026-10-15T09:15:00Z',
        user: 'analyst-nafis',
        text: 'Attempting callback verification to customer alternative contact.',
      },
    ],
  },
  {
    id: 'CAS-2026-003',
    entityId: 'merch-qr-7788',
    entityType: 'MERCHANT',
    status: 'NEW',
    priority: 72,
    typology: 'R03_QR_CASH_SUBSTITUTE',
    maxRiskScore: 74,
    amountAtRiskPaisa: 42000000, // 4,20,000 Taka
    alertCount: 8,
    createdAt: '2026-10-15T09:40:00Z',
    assignee: 'Unassigned',
    phone: '+8801933445566',
    nid: '1976451298734',
    reasonCodes: [
      {
        code: 'R03_QR_CASH_SUBSTITUTE',
        textEn: 'Retail merchant QR receiving uniform round amounts (25,000 BDT) at night hours',
        textBn: 'ব্যবসায়িক কিউআরে রাতের বেলা নির্দিষ্ট অঙ্কের (২৫,০০০ টাকা) পুনরাবৃত্ত লেনদেন',
        weight: 28,
      },
      {
        code: 'R05_STRUCTURING_NEAR_LIMIT',
        textEn: 'Successive transactions positioned exactly 200 BDT below BB reporting threshold',
        textBn: 'বাংলাদেশ ব্যাংকের রিপোর্টিং সীমার ঠিক নিচে ধারাবাহিক লেনদেন',
        weight: 20,
      },
    ],
    timeline: [
      { time: '2026-10-15T02:10:00Z', event: 'First suspicious merchant payment 24,800 BDT' },
      { time: '2026-10-15T03:45:00Z', event: 'Fifth consecutive payment from identical IP subnet' },
    ],
    comments: [],
  },
];

export const mockApprovals = [
  {
    id: 'APP-901',
    actionType: 'FREEZE_ACCOUNT',
    entityId: 'acc-01711223344',
    amountPaisa: 28500000,
    requesterId: 'analyst-nafis',
    requestedAt: '2026-10-15T09:00:00Z',
    status: 'PENDING',
    reason: 'Pass-through mule ring seed node identified. Emergency temporary hold requested.',
  },
  {
    id: 'APP-902',
    actionType: 'RULE_VERSION_ACTIVATION',
    entityId: 'R01_PASS_THROUGH_MULE_v2',
    amountPaisa: 0,
    requesterId: 'analyst-sakib',
    requestedAt: '2026-10-15T08:10:00Z',
    status: 'PENDING',
    reason: 'Backtest demonstrated 94.2% precision with 18% false positive reduction on 30-day replay.',
  },
];

export const mockGraphData = {
  nodes: [
    { id: 'acc-01711223344', label: 'Seed Mule', type: 'ACCOUNT' as const, riskScore: 92, hopDistance: 0 },
    { id: 'dev-imei-4491', label: 'IMEI 4491', type: 'DEVICE' as const, riskScore: 85, hopDistance: 1 },
    { id: 'agent-mirpur-04', label: 'Cashout Agent', type: 'AGENT' as const, riskScore: 68, hopDistance: 1 },
    { id: 'acc-01799001122', label: 'Layer 2 Sender', type: 'ACCOUNT' as const, riskScore: 74, hopDistance: 1 },
    { id: 'acc-01788776655', label: 'Ring Node B', type: 'ACCOUNT' as const, riskScore: 88, hopDistance: 2 },
    { id: 'dev-imei-9902', label: 'IMEI 9902', type: 'DEVICE' as const, riskScore: 78, hopDistance: 2 },
  ],
  edges: [
    { source: 'acc-01799001122', target: 'acc-01711223344', weight: 45000, label: '৳৪৫,০০০', isHighRisk: true },
    { source: 'acc-01711223344', target: 'agent-mirpur-04', weight: 280000, label: '৳২,৮০,০০০', isHighRisk: true },
    { source: 'acc-01711223344', target: 'dev-imei-4491', weight: 1, label: 'Logged On', isHighRisk: true },
    { source: 'acc-01788776655', target: 'dev-imei-4491', weight: 1, label: 'Shared IMEI', isHighRisk: true },
    { source: 'acc-01788776655', target: 'dev-imei-9902', weight: 1, label: 'Secondary Dev', isHighRisk: false },
  ],
};

export interface MockDispute {
  id: string;
  txnId: string;
  senderAccountId: string;
  counterpartyId: string;
  disputeType: 'FAILED_TXN' | 'WRONG_TRANSFER' | 'MERCHANT_DISPUTE' | 'UNAUTHORIZED' | 'QR_TAMPERING';
  amountPaisa: number;
  status: 'SUBMITTED' | 'UNDER_INVESTIGATION' | 'EVIDENCE_REQUESTED' | 'RESOLVED_REFUNDED' | 'RESOLVED_REJECTED';
  createdAt: string;
  deadlineUtc: string;
  warningLevel: 'NORMAL' | 'WARNING_50' | 'WARNING_80' | 'BREACHED';
  filingSource: 'CUSTOMER' | 'OFFICER' | 'AUTO_REFUND';
  note: string;
  resolutionOutcome?: string;
}

export const mockDisputes: MockDispute[] = [
  {
    id: 'DSP-2026-101',
    txnId: 'TXN-ATM-8812',
    senderAccountId: 'acc-01711223344',
    counterpartyId: 'ATM-GULSHAN-01',
    disputeType: 'FAILED_TXN',
    amountPaisa: 1500000, // 15,000 Taka
    status: 'UNDER_INVESTIGATION',
    createdAt: '2026-10-14T10:00:00Z',
    deadlineUtc: '2026-10-18T18:00:00Z', // 3 Bangladesh working days
    warningLevel: 'WARNING_50',
    filingSource: 'CUSTOMER',
    note: 'Money debited but cash dispenser jammed at ATM',
  },
  {
    id: 'DSP-2026-102',
    txnId: 'TXN-P2P-9921',
    senderAccountId: 'acc-01822334455',
    counterpartyId: 'acc-01788776655',
    disputeType: 'UNAUTHORIZED',
    amountPaisa: 25000000, // 2,50,000 Taka
    status: 'SUBMITTED',
    createdAt: '2026-10-15T08:30:00Z',
    deadlineUtc: '2026-11-14T18:00:00Z', // 30 working days
    warningLevel: 'NORMAL',
    filingSource: 'CUSTOMER',
    note: 'Account takeover after phone stolen in Motijheel',
  },
  {
    id: 'DSP-2026-103',
    txnId: 'TXN-AUTO-5544',
    senderAccountId: 'acc-01933445566',
    counterpartyId: 'MERCHANT-BASHUNDHARA',
    disputeType: 'FAILED_TXN',
    amountPaisa: 320000, // 3,200 Taka
    status: 'RESOLVED_REFUNDED',
    createdAt: '2026-10-15T11:00:00Z',
    deadlineUtc: '2026-10-15T11:05:00Z',
    warningLevel: 'NORMAL',
    filingSource: 'AUTO_REFUND',
    resolutionOutcome: 'REFUND_FULL',
    note: 'Debited-but-failed merchant QR payment automatically reversed by auto-refund worker',
  },
];
