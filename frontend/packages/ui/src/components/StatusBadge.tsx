import React from 'react';

export type EntityStatus =
  | 'NEW'
  | 'INVESTIGATING'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'CLOSED'
  | 'ACTIVE'
  | 'ARCHIVED';

export interface StatusBadgeProps {
  status: EntityStatus | string;
  className?: string;
}

const STATUS_CONFIG: Record<string, { label: string; bgClass: string; textClass: string }> = {
  NEW: { label: 'New', bgClass: 'bg-blue-500/10', textClass: 'text-blue-400' },
  INVESTIGATING: { label: 'Investigating', bgClass: 'bg-amber-500/10', textClass: 'text-amber-400' },
  PENDING_APPROVAL: { label: 'Pending Approval', bgClass: 'bg-yellow-500/10', textClass: 'text-yellow-400' },
  APPROVED: { label: 'Approved', bgClass: 'bg-emerald-500/10', textClass: 'text-emerald-400' },
  REJECTED: { label: 'Rejected', bgClass: 'bg-red-500/10', textClass: 'text-red-400' },
  CLOSED: { label: 'Closed', bgClass: 'bg-slate-500/10', textClass: 'text-slate-400' },
  ACTIVE: { label: 'Active', bgClass: 'bg-emerald-500/10', textClass: 'text-emerald-400' },
  ARCHIVED: { label: 'Archived', bgClass: 'bg-slate-600/10', textClass: 'text-slate-500' },
};

export function StatusBadge({ status, className = '' }: StatusBadgeProps) {
  const cfg = STATUS_CONFIG[status] || {
    label: status,
    bgClass: 'bg-slate-500/10',
    textClass: 'text-slate-400',
  };

  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border border-current/20 ${cfg.bgClass} ${cfg.textClass} ${className}`}
    >
      {cfg.label}
    </span>
  );
}
