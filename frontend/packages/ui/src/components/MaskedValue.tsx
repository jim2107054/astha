'use client';

import React, { useState } from 'react';

export interface MaskedValueProps {
  value: string;
  maskedPlaceholder?: string;
  onReveal?: (reason: string) => Promise<string | void> | void;
  label?: string;
}

export function MaskedValue({ value, maskedPlaceholder, onReveal, label = 'PII' }: MaskedValueProps) {
  const [revealed, setRevealed] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [reason, setReason] = useState('');
  const [revealedValue, setRevealedValue] = useState(value);

  const defaultMasked =
    maskedPlaceholder ||
    (value.startsWith('+880') && value.length >= 10
      ? `+880****${value.slice(-4)}`
      : value.length > 6
      ? `${value.slice(0, 3)}****${value.slice(-3)}`
      : '******');

  const handleRevealSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) return;

    if (onReveal) {
      const result = await onReveal(reason);
      if (typeof result === 'string') {
        setRevealedValue(result);
      }
    }
    setRevealed(true);
    setShowModal(false);

    // Auto-hide after 30 seconds for security
    setTimeout(() => {
      setRevealed(false);
      setReason('');
    }, 30000);
  };

  return (
    <div className="inline-flex items-center gap-2 font-mono text-sm">
      <span className={revealed ? 'text-teal-700 dark:text-teal-400 font-semibold' : 'text-slate-500'}>
        {revealed ? revealedValue : defaultMasked}
      </span>

      {!revealed && (
        <button
          type="button"
          onClick={() => setShowModal(true)}
          className="text-xs px-2 py-0.5 rounded bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-300 transition-colors"
          title={`Reveal ${label}`}
        >
          👁 Reveal
        </button>
      )}

      {revealed && (
        <span className="text-[10px] text-amber-600 bg-amber-50 px-1 rounded border border-amber-200">
          Temporary (30s)
        </span>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white dark:bg-slate-900 rounded-lg shadow-xl max-w-md w-full p-6 border border-slate-200 dark:border-slate-800">
            <h3 className="text-lg font-bold text-slate-900 dark:text-slate-100 mb-2">
              Audited PII Reveal Request
            </h3>
            <p className="text-xs text-slate-600 dark:text-slate-400 mb-4">
              Access to unmasked customer identifiers is strictly logged in the append-only audit trail. State your operational justification:
            </p>

            <form onSubmit={handleRevealSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Justification / Case ID <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Investigating CID complaint #4928"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-teal-600"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-3 py-1.5 text-xs font-medium rounded border border-slate-300 text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!reason.trim()}
                  className="px-3 py-1.5 text-xs font-medium rounded bg-teal-600 hover:bg-teal-700 text-white disabled:opacity-50"
                >
                  Confirm & Audit
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
