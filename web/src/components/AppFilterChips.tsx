import React from 'react';
import { AppSummary } from '../types';

interface AppFilterChipsProps {
  summaries: AppSummary[];
  selectedPackage: string | null;
  totalSessionsCount: number;
  onSelect: (pkg: string | null) => void;
}

export const AppFilterChips: React.FC<AppFilterChipsProps> = ({
  summaries,
  selectedPackage,
  totalSessionsCount,
  onSelect,
}) => {
  return (
    <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-2 px-1">
      {/* All Apps Chip */}
      <button
        onClick={() => onSelect(null)}
        className={`flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all duration-150 ${
          selectedPackage === null
            ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
            : 'bg-[#111827] text-slate-400 hover:text-slate-200 border border-slate-800'
        }`}
      >
        <span>All Apps</span>
        <span
          className={`px-1.5 py-0.5 rounded-full text-[10px] ${
            selectedPackage === null ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-300'
          }`}
        >
          {totalSessionsCount}
        </span>
      </button>

      {/* App specific chips */}
      {summaries.map(summary => {
        const isSelected = selectedPackage === summary.packageName;
        return (
          <button
            key={summary.packageName}
            onClick={() => onSelect(isSelected ? null : summary.packageName)}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all duration-150 ${
              isSelected
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30 ring-2 ring-indigo-400/50'
                : 'bg-[#111827] text-slate-300 hover:text-white border border-slate-800 hover:border-slate-700'
            }`}
          >
            <span
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: summary.color }}
            />
            <span>{summary.appName}</span>
            <span
              className={`px-1.5 py-0.5 rounded-full text-[10px] ${
                isSelected ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-300'
              }`}
            >
              {summary.sessionCount}
            </span>
          </button>
        );
      })}
    </div>
  );
};
