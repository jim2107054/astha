import React from 'react';

export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface RiskBadgeProps {
  score: number;
  level?: RiskLevel;
  showScore?: boolean;
  className?: string;
}

export function getRiskLevel(score: number): RiskLevel {
  if (score >= 80) return 'CRITICAL';
  if (score >= 60) return 'HIGH';
  if (score >= 30) return 'MEDIUM';
  return 'LOW';
}

const BADGE_CONFIG: Record<
  RiskLevel,
  { label: string; icon: string; bgClass: string; textClass: string; borderClass: string }
> = {
  CRITICAL: {
    label: 'Critical Risk',
    icon: '🛑',
    bgClass: 'bg-red-500/10',
    textClass: 'text-red-400',
    borderClass: 'border-red-500/30',
  },
  HIGH: {
    label: 'High Risk',
    icon: '⚠️',
    bgClass: 'bg-amber-500/10',
    textClass: 'text-amber-400',
    borderClass: 'border-amber-500/30',
  },
  MEDIUM: {
    label: 'Medium Risk',
    icon: '⚡',
    bgClass: 'bg-yellow-500/10',
    textClass: 'text-yellow-400',
    borderClass: 'border-yellow-500/30',
  },
  LOW: {
    label: 'Low Risk',
    icon: '✅',
    bgClass: 'bg-emerald-500/10',
    textClass: 'text-emerald-400',
    borderClass: 'border-emerald-500/30',
  },
};

export function RiskBadge({ score, level, showScore = true, className = '' }: RiskBadgeProps) {
  const resolvedLevel = level || getRiskLevel(score);
  const cfg = BADGE_CONFIG[resolvedLevel];

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${cfg.bgClass} ${cfg.textClass} ${cfg.borderClass} ${className}`}
    >
      <span>{cfg.icon}</span>
      <span>{cfg.label}</span>
      {showScore && <span className="font-mono font-bold">({score})</span>}
    </span>
  );
}
