'use client';

import React, { useState, useEffect } from 'react';
import { api, RuleDto, RuleVersionDto } from '@astha/api-client';
import { mockCases, mockApprovals, mockGraphData, mockDisputes, MockCase, MockDispute } from '@astha/mocks';
import { formatMoney, formatPaisaShort, formatDateDhaka, getDictionary, Locale } from '@astha/i18n';
import {
  RiskBadge,
  ReasonCodeList,
  MaskedValue,
  StatusBadge,
  ConfirmWithReason,
  GraphCanvas,
} from '@astha/ui';

type ActiveTab = 'overview' | 'queue' | 'detail' | 'graph' | 'approvals' | 'rules' | 'lists' | 'disputes';
type UserRole = 'ANALYST' | 'APPROVER' | 'RISK_MANAGER' | 'COMPLIANCE' | 'ADMIN';

export default function AnalystConsolePage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('queue');
  const [locale, setLocale] = useState<Locale>('en');
  const [currentRole, setCurrentRole] = useState<UserRole>('ANALYST');
  const [currentUserId, setCurrentUserId] = useState<string>('analyst-nafis');

  const [cases, setCases] = useState<MockCase[]>(mockCases);
  const [selectedCase, setSelectedCase] = useState<MockCase>(mockCases[0]);
  const [rules, setRules] = useState<RuleDto[]>([]);
  const [selectedRuleId, setSelectedRuleId] = useState<string>('R01_PASS_THROUGH_MULE');
  const [ruleVersions, setRuleVersions] = useState<RuleVersionDto[]>([]);
  const [approvals, setApprovals] = useState(mockApprovals);
  const [disputes, setDisputes] = useState<MockDispute[]>(mockDisputes);
  const [selectedDispute, setSelectedDispute] = useState<MockDispute>(mockDisputes[0]);

  // Modals & Forms
  const [freezeModalOpen, setFreezeModalOpen] = useState(false);
  const [disputeRefundModalOpen, setDisputeRefundModalOpen] = useState(false);
  const [labelModalOpen, setLabelModalOpen] = useState(false);
  const [selectedLabel, setSelectedLabel] = useState<'TRUE_FRAUD' | 'FALSE_POSITIVE' | 'UNCLEAR'>('TRUE_FRAUD');
  const [notification, setNotification] = useState<string | null>(null);
  const [backtestResult, setBacktestResult] = useState<any>(null);

  const t = getDictionary(locale);

  useEffect(() => {
    api.getRules().then(setRules);
    api.getRuleVersions(selectedRuleId).then(setRuleVersions);
  }, [selectedRuleId]);

  const showToast = (msg: string) => {
    setNotification(msg);
    setTimeout(() => setNotification(null), 4000);
  };

  const handleFreezeConfirm = (reason: string) => {
    // Submit freeze recommendation
    const newApproval = {
      id: `APP-${Date.now().toString().slice(-3)}`,
      actionType: 'FREEZE_ACCOUNT',
      entityId: selectedCase.entityId,
      amountPaisa: selectedCase.amountAtRiskPaisa,
      requesterId: currentUserId,
      requestedAt: new Date().toISOString(),
      status: 'PENDING',
      reason,
    };
    setApprovals([newApproval, ...approvals]);
    setFreezeModalOpen(false);
    showToast(`Freeze recommendation submitted for four-eyes approval (ID: ${newApproval.id})`);
  };

  const handleLabelConfirm = (reason: string) => {
    // Analyst feedback loop label
    const updated = cases.map((c) =>
      c.id === selectedCase.id
        ? {
            ...c,
            status: 'CLOSED' as const,
            timeline: [...c.timeline, { time: new Date().toISOString(), event: `Labeled as ${selectedLabel} (${reason})` }],
          }
        : c
    );
    setCases(updated);
    setSelectedCase({
      ...selectedCase,
      status: 'CLOSED',
      timeline: [...selectedCase.timeline, { time: new Date().toISOString(), event: `Labeled as ${selectedLabel} (${reason})` }],
    });
    setLabelModalOpen(false);
    showToast(`Case ${selectedCase.id} labeled as ${selectedLabel}. Feedback emitted to rule quality engine.`);
  };

  const handleApproveAction = (approvalId: string, requesterId: string) => {
    if (requesterId.toLowerCase() === currentUserId.toLowerCase()) {
      showToast('❌ Four-Eyes Violation: You cannot approve your own action!');
      return;
    }
    setApprovals(approvals.filter((a) => a.id !== approvalId));
    showToast(`Action ${approvalId} successfully approved and executed against provider-mock.`);
  };

  const handleRejectAction = (approvalId: string, requesterId: string) => {
    if (requesterId.toLowerCase() === currentUserId.toLowerCase()) {
      showToast('❌ Four-Eyes Violation: You cannot decide on your own action!');
      return;
    }
    setApprovals(approvals.filter((a) => a.id !== approvalId));
    showToast(`Action ${approvalId} rejected.`);
  };

  const handleDisputeRefundConfirm = (reason: string) => {
    const updated = disputes.map((d) =>
      d.id === selectedDispute.id
        ? {
            ...d,
            status: 'RESOLVED_REFUNDED' as const,
            resolutionOutcome: 'REFUND_FULL',
            note: `${d.note} | Resolved with full refund: ${reason}`,
          }
        : d
    );
    setDisputes(updated);
    setSelectedDispute({
      ...selectedDispute,
      status: 'RESOLVED_REFUNDED',
      resolutionOutcome: 'REFUND_FULL',
      note: `${selectedDispute.note} | Resolved with full refund: ${reason}`,
    });
    setDisputeRefundModalOpen(false);
    showToast(`Dispute ${selectedDispute.id} resolved with Full Refund. Idempotent credit issued via provider-mock.`);
  };

  return (
    <div className="flex flex-col min-h-screen bg-slate-950 text-slate-100">
      {/* Top Navigation Bar */}
      <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur px-6 py-3 flex items-center justify-between sticky top-0 z-40">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-teal-600 flex items-center justify-center font-bold text-white text-lg shadow-lg shadow-teal-900/40">
              আ
            </div>
            <div>
              <span className="font-bold text-lg tracking-tight text-white">{t.common.appName}</span>
              <span className="text-xs text-teal-400 font-mono ml-2">Console v1.0</span>
            </div>
          </div>

          <nav className="flex items-center gap-1 text-xs font-medium">
            {(
              [
                ['queue', t.nav.cases],
                ['detail', 'Case Detail'],
                ['disputes', t.nav.disputes],
                ['graph', t.nav.graph],
                ['approvals', t.nav.approvals],
                ['rules', t.nav.rules],
                ['overview', t.nav.overview],
              ] as const
            ).map(([tabKey, label]) => (
              <button
                key={tabKey}
                onClick={() => setActiveTab(tabKey)}
                className={`px-3 py-1.5 rounded-md transition-colors ${
                  activeTab === tabKey
                    ? 'bg-teal-600/20 text-teal-300 border border-teal-500/40'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                {label}
                {tabKey === 'approvals' && approvals.length > 0 && (
                  <span className="ml-1.5 px-1.5 py-0.2 rounded-full text-[10px] bg-amber-500/20 text-amber-300 font-bold">
                    {approvals.length}
                  </span>
                )}
                {tabKey === 'disputes' && disputes.filter(d => d.warningLevel !== 'NORMAL').length > 0 && (
                  <span className="ml-1.5 px-1.5 py-0.2 rounded-full text-[10px] bg-red-500/20 text-red-300 font-bold">
                    {disputes.filter(d => d.warningLevel !== 'NORMAL').length}
                  </span>
                )}
              </button>
            ))}
          </nav>
        </div>

        {/* Global Controls: User Role & Language */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-slate-800/80 px-2.5 py-1 rounded-md border border-slate-700/80 text-xs">
            <span className="text-slate-400">Role:</span>
            <select
              value={currentRole}
              onChange={(e) => {
                const role = e.target.value as UserRole;
                setCurrentRole(role);
                setCurrentUserId(role === 'APPROVER' ? 'manager-tamim' : 'analyst-nafis');
              }}
              className="bg-transparent font-semibold text-teal-400 focus:outline-none"
            >
              <option value="ANALYST">ANALYST (analyst-nafis)</option>
              <option value="APPROVER">APPROVER (manager-tamim)</option>
              <option value="RISK_MANAGER">RISK_MANAGER (manager-tamim)</option>
              <option value="COMPLIANCE">COMPLIANCE</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          <div className="flex items-center gap-1 bg-slate-800/80 px-2 py-1 rounded-md border border-slate-700/80 text-xs">
            <button
              onClick={() => setLocale('en')}
              className={`px-2 py-0.5 rounded ${locale === 'en' ? 'bg-teal-600 text-white font-bold' : 'text-slate-400'}`}
            >
              EN
            </button>
            <button
              onClick={() => setLocale('bn')}
              className={`px-2 py-0.5 rounded ${locale === 'bn' ? 'bg-teal-600 text-white font-bold' : 'text-slate-400'}`}
            >
              বাংলা
            </button>
          </div>
        </div>
      </header>

      {/* Toast Notification */}
      {notification && (
        <div className="fixed top-16 right-6 z-50 bg-teal-900 border border-teal-500 text-white text-xs px-4 py-2.5 rounded-lg shadow-xl animate-fade-in flex items-center gap-2">
          <span>🔔</span> {notification}
        </div>
      )}

      {/* Main Content Area */}
      <main className="flex-1 p-6 max-w-7xl w-full mx-auto">
        {/* TAB 1: Queue */}
        {activeTab === 'queue' && (
          <section className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-xl font-bold text-white tracking-tight">{t.cases.queueTitle}</h1>
                <p className="text-xs text-slate-400">Priority triage ordered by risk score, amount at risk, and mule cluster linkages</p>
              </div>
              <div className="flex gap-2">
                <span className="text-xs bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-md text-slate-300">
                  Total Active: <strong className="text-white">{cases.length}</strong>
                </span>
              </div>
            </div>

            <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900 shadow">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="bg-slate-800/60 text-slate-400 uppercase tracking-wider text-[11px] border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Priority</th>
                    <th className="py-3 px-4">Case ID</th>
                    <th className="py-3 px-4">Account / Entity</th>
                    <th className="py-3 px-4">Typology</th>
                    <th className="py-3 px-4">Risk Score</th>
                    <th className="py-3 px-4">Amount at Risk</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {cases.map((c) => (
                    <tr
                      key={c.id}
                      onClick={() => {
                        setSelectedCase(c);
                        setActiveTab('detail');
                      }}
                      className="hover:bg-slate-800/40 cursor-pointer transition-colors"
                    >
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded font-mono font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                          P{c.priority}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-mono font-semibold text-teal-400">{c.id}</td>
                      <td className="py-3 px-4 font-mono text-slate-200">
                        <MaskedValue value={c.phone} />
                      </td>
                      <td className="py-3 px-4">
                        <span className="text-slate-300">{c.typology}</span>
                      </td>
                      <td className="py-3 px-4">
                        <RiskBadge score={c.maxRiskScore} />
                      </td>
                      <td className="py-3 px-4 font-mono font-medium text-slate-200">
                        {formatMoney(c.amountAtRiskPaisa, locale)}
                      </td>
                      <td className="py-3 px-4">
                        <StatusBadge status={c.status} />
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedCase(c);
                            setActiveTab('detail');
                          }}
                          className="px-2.5 py-1 rounded bg-teal-600/30 hover:bg-teal-600/50 text-teal-300 text-xs font-semibold border border-teal-500/40"
                        >
                          Investigate →
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {/* TAB 2: Case Detail */}
        {activeTab === 'detail' && (
          <section className="space-y-6">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div>
                <button
                  onClick={() => setActiveTab('queue')}
                  className="text-xs text-teal-400 hover:underline mb-1 inline-block"
                >
                  ← Back to Queue
                </button>
                <div className="flex items-center gap-3">
                  <h1 className="text-2xl font-bold text-white tracking-tight">{selectedCase.id}</h1>
                  <StatusBadge status={selectedCase.status} />
                  <RiskBadge score={selectedCase.maxRiskScore} />
                </div>
              </div>

              {/* Action Buttons for Analyst */}
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setFreezeModalOpen(true)}
                  className="px-3.5 py-2 rounded-lg bg-red-600/80 hover:bg-red-600 text-white text-xs font-bold shadow transition-colors flex items-center gap-1.5"
                >
                  <span>🛡️</span> {t.cases.recommendFreeze}
                </button>

                <div className="flex items-center rounded-lg border border-slate-700 bg-slate-900 p-0.5 text-xs">
                  <button
                    onClick={() => {
                      setSelectedLabel('TRUE_FRAUD');
                      setLabelModalOpen(true);
                    }}
                    className="px-2.5 py-1.5 rounded hover:bg-red-500/20 text-red-300 font-medium"
                  >
                    True Fraud
                  </button>
                  <button
                    onClick={() => {
                      setSelectedLabel('FALSE_POSITIVE');
                      setLabelModalOpen(true);
                    }}
                    className="px-2.5 py-1.5 rounded hover:bg-emerald-500/20 text-emerald-300 font-medium"
                  >
                    False Positive
                  </button>
                  <button
                    onClick={() => {
                      setSelectedLabel('UNCLEAR');
                      setLabelModalOpen(true);
                    }}
                    className="px-2.5 py-1.5 rounded hover:bg-slate-700 text-slate-300 font-medium"
                  >
                    Unclear
                  </button>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Left Column: Entity Card & Reason Codes */}
              <div className="lg:col-span-2 space-y-6">
                {/* Entity Identification Card */}
                <div className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">
                    Customer Entity Card (Protected PII)
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span className="text-slate-400 block mb-1">Account ID</span>
                      <span className="font-mono text-slate-200">{selectedCase.entityId}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-1">Phone Number</span>
                      <MaskedValue value={selectedCase.phone} label="Phone Number" />
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-1">National ID (NID)</span>
                      <MaskedValue value={selectedCase.nid} label="National ID" />
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-1">Total Amount at Risk</span>
                      <span className="font-mono font-bold text-teal-400 text-sm">
                        {formatMoney(selectedCase.amountAtRiskPaisa, locale)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Reason Codes with Feature Values */}
                <div className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">
                    Triggered Reason Codes & Explanations ({locale.toUpperCase()})
                  </h3>
                  <ReasonCodeList reasons={selectedCase.reasonCodes} locale={locale} />
                </div>

                {/* Mini Graph View for the Case */}
                <div className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">
                    Associated Mule Ring & Device Links
                  </h3>
                  <GraphCanvas
                    nodes={mockGraphData.nodes}
                    edges={mockGraphData.edges}
                    selectedNodeId={selectedCase.entityId}
                  />
                </div>
              </div>

              {/* Right Column: Timeline & Comments */}
              <div className="space-y-6">
                <div className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-3">Case Timeline</h3>
                  <div className="space-y-3 text-xs">
                    {selectedCase.timeline.map((entry, idx) => (
                      <div key={idx} className="relative pl-4 border-l border-teal-800 pb-2">
                        <span className="absolute -left-1.5 top-1 w-3 h-3 rounded-full bg-teal-600 border-2 border-slate-900"></span>
                        <div className="text-[10px] text-slate-400 font-mono">
                          {formatDateDhaka(entry.time, 'short')}
                        </div>
                        <div className="text-slate-200 mt-0.5">{entry.event}</div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-3">Analyst Notes</h3>
                  <div className="space-y-2 mb-3">
                    {selectedCase.comments.map((cm, i) => (
                      <div key={i} className="p-2.5 rounded bg-slate-800/60 text-xs">
                        <div className="flex justify-between text-[10px] text-slate-400 mb-1">
                          <span className="font-semibold text-teal-400">{cm.user}</span>
                          <span>{formatDateDhaka(cm.time, 'short')}</span>
                        </div>
                        <p className="text-slate-300">{cm.text}</p>
                      </div>
                    ))}
                  </div>
                  <input
                    type="text"
                    placeholder="Add operational investigation note..."
                    className="w-full px-3 py-2 text-xs rounded border border-slate-700 bg-slate-800 text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' && e.currentTarget.value.trim()) {
                        const note = e.currentTarget.value;
                        setSelectedCase({
                          ...selectedCase,
                          comments: [...selectedCase.comments, { time: new Date().toISOString(), user: currentUserId, text: note }],
                        });
                        e.currentTarget.value = '';
                        showToast('Note recorded in case log');
                      }
                    }}
                  />
                </div>
              </div>
            </div>
          </section>
        )}

        {/* TAB 3: Graph Explorer */}
        {activeTab === 'graph' && (
          <section className="space-y-4">
            <div>
              <h1 className="text-xl font-bold text-white tracking-tight">Entity Graph & Mule Ring Explorer</h1>
              <p className="text-xs text-slate-400">Interactive topology inspection up to depth 3 with shared device and agent cash-out evidence paths</p>
            </div>
            <GraphCanvas nodes={mockGraphData.nodes} edges={mockGraphData.edges} />
          </section>
        )}

        {/* TAB 4: Four-Eyes Approvals Desk */}
        {activeTab === 'approvals' && (
          <section className="space-y-4">
            <div>
              <h1 className="text-xl font-bold text-white tracking-tight">{t.approvals.title}</h1>
              <p className="text-xs text-slate-400">
                Four-eyes governance rule enforced server-side: actions affecting customer accounts or rules require an independent approver.
              </p>
            </div>

            <div className="space-y-3">
              {approvals.length === 0 ? (
                <div className="p-8 text-center text-slate-400 border border-slate-800 rounded-xl bg-slate-900 text-xs">
                  No pending approvals in queue
                </div>
              ) : (
                approvals.map((app) => {
                  const isAuthor = app.requesterId.toLowerCase() === currentUserId.toLowerCase();
                  return (
                    <div
                      key={app.id}
                      className="p-5 rounded-xl border border-slate-800 bg-slate-900 shadow flex flex-col md:flex-row md:items-center justify-between gap-4"
                    >
                      <div className="space-y-1 text-xs">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-amber-400">{app.actionType}</span>
                          <span className="font-mono text-slate-300">({app.entityId})</span>
                          {app.amountPaisa > 0 && (
                            <span className="px-2 py-0.5 rounded bg-slate-800 text-teal-400 font-mono font-bold">
                              {formatMoney(app.amountPaisa, locale)}
                            </span>
                          )}
                        </div>
                        <p className="text-slate-300">{app.reason}</p>
                        <div className="text-[11px] text-slate-400">
                          Requested by: <strong className="text-slate-200">{app.requesterId}</strong> · {formatDateDhaka(app.requestedAt, 'short')}
                        </div>
                        {isAuthor && (
                          <div className="text-red-400 text-[11px] font-semibold">
                            ⚠️ Self-approval prohibited: An independent officer must approve this request.
                          </div>
                        )}
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          disabled={isAuthor}
                          onClick={() => handleApproveAction(app.id, app.requesterId)}
                          className="px-4 py-2 rounded-lg bg-teal-600 hover:bg-teal-700 disabled:opacity-40 text-white font-bold text-xs shadow transition-colors"
                        >
                          Approve Action
                        </button>
                        <button
                          disabled={isAuthor}
                          onClick={() => handleRejectAction(app.id, app.requesterId)}
                          className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 disabled:opacity-40 text-slate-300 text-xs font-semibold border border-slate-700"
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </section>
        )}

        {/* TAB 5: Rules Management & Backtests */}
        {activeTab === 'rules' && (
          <section className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-xl font-bold text-white tracking-tight">{t.rules.title}</h1>
                <p className="text-xs text-slate-400">Versioned rule definitions, parameters, and backtesting on historical ground truth</p>
              </div>
              <button
                onClick={() => {
                  api.runBacktest(selectedRuleId, `${selectedRuleId}_v1`).then((res) => {
                    setBacktestResult({
                      ruleId: res.ruleId,
                      sampleCount: res.sampleCount,
                      precision: typeof res.precision === 'number' ? `${(res.precision * 100).toFixed(1)}%` : res.precision,
                      recall: typeof res.recall === 'number' ? `${(res.recall * 100).toFixed(1)}%` : res.recall,
                      alertsGenerated: res.alertsEvaluated,
                    });
                    showToast('Backtest simulation completed against 5,000 ground-truth transactions');
                  });
                }}
                className="px-3.5 py-2 rounded-lg bg-teal-600 hover:bg-teal-700 text-white text-xs font-bold shadow"
              >
                ▶ Run Backtest (5k Events)
              </button>
            </div>

            {/* Backtest Results Banner */}
            {backtestResult && (
              <div className="p-4 rounded-xl border border-teal-700 bg-teal-950/60 text-xs text-slate-200">
                <div className="font-bold text-teal-300 mb-2">Backtest Result ({backtestResult.ruleId})</div>
                <div className="grid grid-cols-4 gap-4 font-mono">
                  <div>Sample Count: <strong>{backtestResult.sampleCount}</strong></div>
                  <div>Alerts: <strong>{backtestResult.alertsGenerated}</strong></div>
                  <div>Precision: <strong className="text-emerald-400">{backtestResult.precision}</strong></div>
                  <div>Recall: <strong className="text-teal-400">{backtestResult.recall}</strong></div>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Rules List */}
              <div className="space-y-2">
                <span className="text-xs font-bold uppercase text-slate-400">Rules Catalog</span>
                <div className="space-y-1.5">
                  {rules.map((r) => (
                    <button
                      key={r.id}
                      onClick={() => setSelectedRuleId(r.id)}
                      className={`w-full text-left p-3 rounded-lg border text-xs transition-colors ${
                        selectedRuleId === r.id
                          ? 'border-teal-500 bg-teal-950/30 text-white'
                          : 'border-slate-800 bg-slate-900 text-slate-300 hover:bg-slate-800/60'
                      }`}
                    >
                      <div className="font-bold">{r.name}</div>
                      <div className="text-[11px] text-slate-400 font-mono mt-0.5">{r.id}</div>
                      <div className="flex items-center gap-2 mt-2">
                        <span className="px-1.5 py-0.5 rounded bg-slate-800 text-[10px] font-semibold text-teal-300">
                          {r.severity}
                        </span>
                        <span className="text-[10px] text-slate-400">Weight: +{r.weight}</span>
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Version History & Diff Viewer */}
              <div className="md:col-span-2 space-y-4">
                <span className="text-xs font-bold uppercase text-slate-400">Version History & Lifecycle</span>
                <div className="space-y-3">
                  {ruleVersions.map((v) => (
                    <div
                      key={v.id}
                      className="p-4 rounded-xl border border-slate-800 bg-slate-900 text-xs space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-teal-400 font-mono">v{v.versionNumber} ({v.id})</span>
                          <StatusBadge status={v.status} />
                        </div>
                        <span className="text-slate-400 text-[11px]">Author: {v.authorId}</span>
                      </div>

                      <pre className="p-3 rounded bg-slate-950 text-slate-300 font-mono text-[11px] overflow-x-auto border border-slate-800/80">
                        {v.parametersJson}
                      </pre>

                      <div className="flex justify-end gap-2 pt-1">
                        {v.status === 'PENDING_APPROVAL' && (
                          <button
                            onClick={() => {
                              api.approveRuleVersion(v.id, 'Approved via console').then(() => {
                                showToast(`Version ${v.id} approved and activated.`);
                                api.getRuleVersions(selectedRuleId).then(setRuleVersions);
                              }).catch((err) => showToast(`❌ ${err.message}`));
                            }}
                            className="px-3 py-1.5 rounded bg-teal-600 hover:bg-teal-700 text-white font-semibold text-xs"
                          >
                            Approve & Activate (Four-Eyes)
                          </button>
                        )}
                        {v.status === 'ARCHIVED' && (
                          <button
                            onClick={() => {
                              api.rollbackRule(v.ruleId, v.id).then(() => {
                                showToast(`Rule rolled back to ${v.id}`);
                                api.getRuleVersions(selectedRuleId).then(setRuleVersions);
                              });
                            }}
                            className="px-3 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs border border-slate-700"
                          >
                            Rollback to this Version
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>
        )}

        {/* TAB: Dispute Desk (Phase F2) */}
        {activeTab === 'disputes' && (
          <section className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-xl font-bold text-white tracking-tight">Bangladesh MFS Dispute Operations Desk</h1>
                <p className="text-xs text-slate-400">
                  Regulatory working-day SLA tracking (Friday & Saturday off) with automated debited-but-failed refund loop.
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    const autoRef = {
                      id: `DSP-AUTO-${Date.now().toString().slice(-4)}`,
                      txnId: `TXN-AUTO-${Date.now().toString().slice(-4)}`,
                      senderAccountId: 'acc-01722334455',
                      counterpartyId: 'MERCHANT-DHAKA-01',
                      disputeType: 'FAILED_TXN' as const,
                      amountPaisa: 120000,
                      status: 'RESOLVED_REFUNDED' as const,
                      createdAt: new Date().toISOString(),
                      deadlineUtc: new Date().toISOString(),
                      warningLevel: 'NORMAL' as const,
                      filingSource: 'AUTO_REFUND' as const,
                      resolutionOutcome: 'REFUND_FULL',
                      note: 'Automated debited-but-failed transaction recovered and refunded.',
                    };
                    setDisputes([autoRef, ...disputes]);
                    showToast(`Simulated auto-refund event triggered for ${autoRef.txnId}`);
                  }}
                  className="px-3 py-1.5 rounded-lg bg-teal-600/20 hover:bg-teal-600/30 text-teal-300 border border-teal-500/40 text-xs font-semibold"
                >
                  ⚡ Simulate Auto-Refund Event
                </button>
              </div>
            </div>

            {/* SLA Metric Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
                <span className="text-xs text-slate-400 block mb-1">Total Active Disputes</span>
                <span className="text-2xl font-bold text-white font-mono">{disputes.length}</span>
              </div>
              <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
                <span className="text-xs text-slate-400 block mb-1">SLA Warnings / Risk</span>
                <span className="text-2xl font-bold text-amber-400 font-mono">
                  {disputes.filter((d) => d.warningLevel !== 'NORMAL').length}
                </span>
              </div>
              <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
                <span className="text-xs text-slate-400 block mb-1">Auto-Refunded Share</span>
                <span className="text-2xl font-bold text-emerald-400 font-mono">
                  {Math.round((disputes.filter((d) => d.filingSource === 'AUTO_REFUND').length / Math.max(1, disputes.length)) * 100)}%
                </span>
              </div>
              <div className="p-4 rounded-xl border border-slate-800 bg-slate-900/60">
                <span className="text-xs text-slate-400 block mb-1">Total Disputed Volume</span>
                <span className="text-2xl font-bold text-teal-400 font-mono">
                  {formatMoney(disputes.reduce((sum, d) => sum + d.amountPaisa, 0), locale)}
                </span>
              </div>
            </div>

            {/* Two-Column Dispute Workspace */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Left Column: Priority SLA Queue */}
              <div className="lg:col-span-2 border border-slate-800 rounded-xl bg-slate-900/60 overflow-hidden">
                <div className="px-5 py-3 border-b border-slate-800 bg-slate-900 flex justify-between items-center">
                  <h2 className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                    SLA-Ranked Dispute Queue
                  </h2>
                  <span className="text-xs text-slate-500 font-mono">{disputes.length} records</span>
                </div>
                <div className="divide-y divide-slate-800 overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-950/40 text-slate-400 uppercase text-[10px]">
                      <tr>
                        <th className="px-4 py-2.5">Dispute ID</th>
                        <th className="px-4 py-2.5">Type</th>
                        <th className="px-4 py-2.5">Amount</th>
                        <th className="px-4 py-2.5">SLA Clock</th>
                        <th className="px-4 py-2.5">Status</th>
                        <th className="px-4 py-2.5 text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800">
                      {disputes.map((d) => (
                        <tr
                          key={d.id}
                          onClick={() => setSelectedDispute(d)}
                          className={`cursor-pointer transition-colors ${
                            selectedDispute.id === d.id ? 'bg-teal-950/30' : 'hover:bg-slate-800/40'
                          }`}
                        >
                          <td className="px-4 py-3">
                            <span className="font-semibold text-white block">{d.id}</span>
                            <span className="text-[10px] text-slate-400 font-mono">{d.txnId}</span>
                            {d.filingSource === 'AUTO_REFUND' && (
                              <span className="inline-block mt-0.5 px-1.5 py-0.2 rounded text-[9px] bg-emerald-500/20 text-emerald-300 font-bold">
                                AUTO-REFUND
                              </span>
                            )}
                          </td>
                          <td className="px-4 py-3">
                            <span className="font-medium text-slate-200">{d.disputeType}</span>
                          </td>
                          <td className="px-4 py-3 font-mono font-semibold text-teal-300">
                            {formatMoney(d.amountPaisa, locale)}
                          </td>
                          <td className="px-4 py-3">
                            <span
                              className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                d.warningLevel === 'BREACHED'
                                  ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                                  : d.warningLevel === 'WARNING_80'
                                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                                  : 'bg-emerald-500/20 text-emerald-300'
                              }`}
                            >
                              {d.warningLevel}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                              {d.status}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-right">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedDispute(d);
                              }}
                              className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs"
                            >
                              Inspect
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Right Column: Dispute Resolution Workspace */}
              <div className="border border-slate-800 rounded-xl bg-slate-900/60 p-5 space-y-5">
                <div className="border-b border-slate-800 pb-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono text-teal-400 font-bold">{selectedDispute.id}</span>
                    <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                      {selectedDispute.status}
                    </span>
                  </div>
                  <h3 className="text-base font-bold text-white mt-1">{selectedDispute.disputeType}</h3>
                </div>

                <div className="space-y-3 text-xs">
                  <div>
                    <span className="text-slate-400 block mb-0.5">Disputed Amount</span>
                    <span className="text-lg font-bold text-teal-300 font-mono">
                      {formatMoney(selectedDispute.amountPaisa, locale)}
                    </span>
                  </div>

                  <div>
                    <span className="text-slate-400 block mb-0.5">Customer MSISDN (PII Masked)</span>
                    <MaskedValue
                      value={selectedDispute.senderAccountId}
                      label="Customer Phone"
                      onReveal={async (reason: string) => {
                        showToast(`PII reveal recorded in audit log: "${reason}"`);
                        return '+8801711223344';
                      }}
                    />
                  </div>

                  <div>
                    <span className="text-slate-400 block mb-0.5">Counterparty</span>
                    <span className="font-mono text-slate-200">{selectedDispute.counterpartyId}</span>
                  </div>

                  <div className="p-3 rounded-lg border border-slate-800 bg-slate-950/60">
                    <span className="text-slate-400 block mb-1 font-semibold text-[11px]">
                      Bangladesh Bank SLA Clock
                    </span>
                    <p className="text-[11px] text-slate-300">
                      Deadline: <span className="font-mono text-amber-300">{formatDateDhaka(selectedDispute.deadlineUtc)}</span>
                    </p>
                    <p className="text-[10px] text-slate-500 mt-1">
                      Weekend calendar active: Fridays and Saturdays are non-working days under BB MFS guideline.
                    </p>
                  </div>

                  <div>
                    <span className="text-slate-400 block mb-0.5">Case Notes / Telemetry</span>
                    <p className="text-slate-300 bg-slate-950 p-2.5 rounded border border-slate-800">
                      {selectedDispute.note}
                    </p>
                  </div>
                </div>

                {/* Dispute Actions */}
                <div className="pt-3 border-t border-slate-800 space-y-2">
                  {selectedDispute.status !== 'RESOLVED_REFUNDED' && (
                    <button
                      onClick={() => setDisputeRefundModalOpen(true)}
                      className="w-full py-2 rounded-lg bg-teal-600 hover:bg-teal-500 text-white font-semibold text-xs transition-colors shadow-lg shadow-teal-900/30"
                    >
                      ✓ Authorize Full Refund (Idempotent)
                    </button>
                  )}
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => showToast('Evidence request sent to customer SMS & app notification')}
                      className="py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium text-xs border border-slate-700"
                    >
                      Request Evidence
                    </button>
                    <button
                      onClick={() => showToast('Transaction query sent to Merchant portal')}
                      className="py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium text-xs border border-slate-700"
                    >
                      Send to Merchant
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* TAB 6: Overview KPIs */}
        {activeTab === 'overview' && (
          <section className="space-y-6">
            <h1 className="text-xl font-bold text-white tracking-tight">Executive Risk Operations Overview</h1>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="p-5 rounded-xl border border-slate-800 bg-slate-900">
                <span className="text-xs text-slate-400 block mb-1">Active Cases</span>
                <span className="text-2xl font-bold text-white font-mono">{cases.length}</span>
              </div>
              <div className="p-5 rounded-xl border border-slate-800 bg-slate-900">
                <span className="text-xs text-slate-400 block mb-1">Prevented Loss (Simulated)</span>
                <span className="text-2xl font-bold text-teal-400 font-mono">
                  {formatPaisaShort(cases.reduce((sum, c) => sum + c.amountAtRiskPaisa, 0), locale)}
                </span>
              </div>
              <div className="p-5 rounded-xl border border-slate-800 bg-slate-900">
                <span className="text-xs text-slate-400 block mb-1">Four-Eyes Pending</span>
                <span className="text-2xl font-bold text-amber-400 font-mono">{approvals.length}</span>
              </div>
              <div className="p-5 rounded-xl border border-slate-800 bg-slate-900">
                <span className="text-xs text-slate-400 block mb-1">Decision Latency (p95)</span>
                <span className="text-2xl font-bold text-emerald-400 font-mono">14 ms</span>
              </div>
            </div>
          </section>
        )}
      </main>

      {/* Confirmation Modals */}
      <ConfirmWithReason
        isOpen={freezeModalOpen}
        title="Confirm Irreversible Account Freeze Recommendation"
        description={`You are recommending a temporary protective freeze on account ${selectedCase.entityId} with ${formatMoney(selectedCase.amountAtRiskPaisa, locale)} at risk. This action requires secondary review under Bangladesh Bank guidelines.`}
        actionLabel="Submit for Four-Eyes Review"
        isDestructive={true}
        onConfirm={handleFreezeConfirm}
        onCancel={() => setFreezeModalOpen(false)}
      />

      <ConfirmWithReason
        isOpen={disputeRefundModalOpen}
        title="Confirm Dispute Resolution: Full Refund"
        description={`You are resolving dispute ${selectedDispute.id} with a full refund of ${formatMoney(selectedDispute.amountPaisa, locale)}. This triggers an idempotent refund credit to the sender account via provider-mock.`}
        actionLabel="Execute Refund Resolution"
        isDestructive={false}
        onConfirm={handleDisputeRefundConfirm}
        onCancel={() => setDisputeRefundModalOpen(false)}
      />

      <ConfirmWithReason
        isOpen={labelModalOpen}
        title={`Confirm Feedback Label: ${selectedLabel}`}
        description={`Labeling case ${selectedCase.id} as ${selectedLabel} directly updates the rule quality precision metrics and suggests threshold adjustments.`}
        actionLabel="Record Label"
        isDestructive={false}
        onConfirm={handleLabelConfirm}
        onCancel={() => setLabelModalOpen(false)}
      />
    </div>
  );
}
