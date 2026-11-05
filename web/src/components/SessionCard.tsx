import React from 'react';
import { AppSession } from '../types';
import { formatTime, formatDuration } from '../mockData';
import { ArrowRight, Clock, Hourglass } from 'lucide-react';

interface SessionCardProps {
  session: AppSession;
  onClickApp?: () => void;
}

export const SessionCard: React.FC<SessionCardProps> = ({ session, onClickApp }) => {
  const durationMs = session.endTime - session.startTime;

  const formatGap = (ms?: number) => {
    if (!ms || ms <= 0) return null;
    const mins = Math.floor(ms / (60 * 1000));
    const hours = Math.floor(mins / 60);
    if (hours > 0) return `${hours}h ${mins % 60}m gap`;
    if (mins > 0) return `${mins}m gap`;
    return `${Math.floor(ms / 1000)}s gap`;
  };

  const gapText = formatGap(session.gapSincePreviousMs);

  return (
    <div
      onClick={onClickApp}
      className="group relative bg-[#111827] hover:bg-[#162035] transition-all duration-200 border border-slate-800/80 hover:border-slate-700 rounded-2xl p-4 shadow-sm hover:shadow-md cursor-pointer"
    >
      {/* Header Row: App info + Duration badge */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div
            className="w-3.5 h-3.5 rounded-full ring-2 ring-slate-800 shadow-sm"
            style={{ backgroundColor: session.color }}
          />
          <span className="font-bold text-white text-base group-hover:text-indigo-300 transition-colors">
            {session.appName}
          </span>
          {session.isActiveNow && (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 animate-pulse">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
              ACTIVE NOW
            </span>
          )}
        </div>

        <div className="px-3 py-1 rounded-full bg-[#090D16] border border-slate-800 text-xs font-semibold text-slate-200">
          {formatDuration(durationMs)}
        </div>
      </div>

      {/* Interval Highlight Box: FROM -> TO */}
      <div className="mt-3 flex items-center justify-between px-3.5 py-2.5 bg-[#090D16] rounded-xl border border-slate-800/60 font-mono text-sm">
        <div className="flex items-center gap-2 text-slate-200">
          <Clock className="w-4 h-4 text-slate-500" />
          <span className="font-semibold text-white tracking-wide">
            {formatTime(session.startTime)}
          </span>
        </div>

        <ArrowRight className="w-4 h-4 text-slate-600" />

        <div className="flex items-center gap-1.5">
          <span
            className={`font-semibold tracking-wide ${
              session.isActiveNow ? 'text-emerald-400 animate-pulse' : 'text-white'
            }`}
          >
            {session.isActiveNow ? 'Current Session' : formatTime(session.endTime)}
          </span>
        </div>
      </div>

      {/* Gap from previous session info */}
      {gapText && (
        <div className="mt-2.5 flex items-center gap-1.5 text-xs text-slate-400 font-medium">
          <Hourglass className="w-3.5 h-3.5 text-slate-500" />
          <span>Opened after <strong className="text-slate-300 font-semibold">{gapText}</strong></span>
        </div>
      )}
    </div>
  );
};
