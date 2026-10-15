'use client';

import React, { useState } from 'react';

export interface ConfirmWithReasonProps {
  isOpen: boolean;
  title: string;
  description: string;
  actionLabel: string;
  isDestructive?: boolean;
  onConfirm: (reason: string) => void;
  onCancel: () => void;
}

export function ConfirmWithReason({
  isOpen,
  title,
  description,
  actionLabel,
  isDestructive = false,
  onConfirm,
  onCancel,
}: ConfirmWithReasonProps) {
  const [reason, setReason] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) return;
    onConfirm(reason);
    setReason('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-lg w-full p-6 shadow-2xl text-slate-100">
        <h3 className="text-lg font-bold text-white mb-2">{title}</h3>
        <p className="text-xs text-slate-400 mb-4 leading-relaxed">{description}</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Operational & Regulatory Justification <span className="text-red-400">*</span>
            </label>
            <textarea
              required
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="State regulatory or operational justification..."
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-700 bg-slate-950 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-xs font-semibold rounded-lg border border-slate-700 hover:bg-slate-800 text-slate-300 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!reason.trim()}
              className={`px-4 py-2 text-xs font-bold rounded-lg text-white transition-colors disabled:opacity-40 ${
                isDestructive
                  ? 'bg-red-600 hover:bg-red-700 shadow-lg shadow-red-900/30'
                  : 'bg-teal-600 hover:bg-teal-700 shadow-lg shadow-teal-900/30'
              }`}
            >
              {actionLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
