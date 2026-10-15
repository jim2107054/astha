import React from 'react';
import { Locale } from '@astha/i18n';

export interface ReasonCodeItem {
  code: string;
  textEn: string;
  textBn: string;
  weight?: number;
}

export interface ReasonCodeListProps {
  reasons: ReasonCodeItem[];
  locale?: Locale;
  className?: string;
}

export function ReasonCodeList({ reasons, locale = 'en', className = '' }: ReasonCodeListProps) {
  if (!reasons || reasons.length === 0) {
    return <span className="text-xs text-slate-500 italic">No triggered reason codes</span>;
  }

  return (
    <div className={`space-y-2 ${className}`}>
      {reasons.map((r, i) => (
        <div
          key={`${r.code}-${i}`}
          className="flex items-start justify-between gap-3 p-2.5 rounded-lg border border-slate-800 bg-slate-900/60"
        >
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold text-teal-400 bg-teal-950/60 px-1.5 py-0.5 rounded border border-teal-800/60">
                {r.code}
              </span>
              {typeof r.weight === 'number' && (
                <span className="text-[10px] text-slate-400 font-mono">Weight: +{r.weight}</span>
              )}
            </div>
            <p className="text-xs text-slate-300">
              {locale === 'bn' ? r.textBn : r.textEn}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
