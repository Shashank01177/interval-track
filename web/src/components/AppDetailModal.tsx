import React from 'react';
import { AppSummary } from '../types';
import { TimelineBar } from './TimelineBar';
import { SessionCard } from './SessionCard';
import { formatDuration } from '../mockData';
import { ArrowLeft, Clock, Layers, Zap, Trophy } from 'lucide-react';

interface AppDetailModalProps {
  summary: AppSummary;
  dayStartMs: number;
  dayEndMs: number;
  onClose: () => void;
}

export const AppDetailModal: React.FC<AppDetailModalProps> = ({
  summary,
  dayStartMs,
  dayEndMs,
  onClose,
}) => {
  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex justify-center items-center p-2 sm:p-4 animate-in fade-in duration-200">
      <div className="bg-[#090D16] border border-slate-800 rounded-3xl w-full max-w-xl max-h-[92vh] flex flex-col overflow-hidden shadow-2xl">
        {/* Top Header */}
        <div className="flex items-center gap-3 p-4 border-b border-slate-800 bg-[#111827]">
          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-3">
            <span
              className="w-4 h-4 rounded-full"
              style={{ backgroundColor: summary.color }}
            />
            <div>
              <h2 className="text-lg font-bold text-white leading-tight">
                {summary.appName}
              </h2>
              <p className="text-xs text-slate-400 font-mono">
                {summary.packageName}
              </p>
            </div>
          </div>
        </div>

        {/* Content Scrollable */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {/* 4 Metric Cards */}
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-3.5">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-medium mb-1">
                <Layers className="w-3.5 h-3.5 text-indigo-400" />
                <span>Total Times Opened</span>
              </div>
              <div className="text-xl font-extrabold text-white">
                {summary.sessionCount} <span className="text-sm font-normal text-slate-400">sessions</span>
              </div>
            </div>

            <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-3.5">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-medium mb-1">
                <Clock className="w-3.5 h-3.5 text-emerald-400" />
                <span>Cumulative Time</span>
              </div>
              <div className="text-xl font-extrabold text-white">
                {formatDuration(summary.totalDurationMs)}
              </div>
            </div>

            <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-3.5">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-medium mb-1">
                <Zap className="w-3.5 h-3.5 text-amber-400" />
                <span>Average Session</span>
              </div>
              <div className="text-xl font-extrabold text-white">
                {formatDuration(summary.avgDurationMs)}
              </div>
            </div>

            <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-3.5">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-medium mb-1">
                <Trophy className="w-3.5 h-3.5 text-rose-400" />
                <span>Longest Session</span>
              </div>
              <div className="text-xl font-extrabold text-white">
                {formatDuration(summary.longestDurationMs)}
              </div>
            </div>
          </div>

          {/* App-specific 24h timeline */}
          <div>
            <div className="text-xs font-semibold text-slate-400 mb-2">
              Timeline Spread
            </div>
            <TimelineBar
              sessions={summary.sessions}
              dayStartMs={dayStartMs}
              dayEndMs={dayEndMs}
              highlightPackage={summary.packageName}
            />
          </div>

          {/* Session Feed */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-bold text-white">
                Recorded Intervals ({summary.sessions.length})
              </h3>
              <span className="text-xs text-slate-500">Most recent first</span>
            </div>
            <div className="space-y-2.5">
              {summary.sessions.map(session => (
                <SessionCard key={session.id} session={session} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
