import React, { useState } from 'react';
import { AppSession } from '../types';
import { formatTimeShort, formatDuration } from '../mockData';

interface TimelineBarProps {
  sessions: AppSession[];
  dayStartMs: number;
  dayEndMs: number;
  highlightPackage?: string | null;
  onSelectHour?: (hour: number | null) => void;
  selectedHour?: number | null;
}

export const TimelineBar: React.FC<TimelineBarProps> = ({
  sessions,
  dayStartMs,
  dayEndMs,
  highlightPackage,
  onSelectHour,
  selectedHour
}) => {
  const [hoveredSession, setHoveredSession] = useState<AppSession | null>(null);
  const totalMs = Math.max(1, dayEndMs - dayStartMs);
  const now = Date.now();
  const isToday = now >= dayStartMs && now <= dayEndMs;

  const currentFraction = isToday ? (now - dayStartMs) / totalMs : null;

  return (
    <div className="bg-[#111827] border border-slate-800 rounded-2xl p-4 shadow-xl">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
          24-Hour Session Timeline
        </span>
        {isToday && (
          <div className="flex items-center gap-1.5 text-xs font-medium text-emerald-400">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Live Progress
          </div>
        )}
      </div>

      {/* Timeline Bar Strip */}
      <div className="relative h-12 bg-[#090D16] border border-slate-800/80 rounded-xl overflow-hidden my-2 group">
        {/* Hour Grid Lines */}
        <div className="absolute inset-0 flex pointer-events-none">
          {Array.from({ length: 24 }).map((_, i) => (
            <div
              key={i}
              className={`flex-1 border-r border-slate-800/40 ${
                selectedHour === i ? 'bg-indigo-500/10' : ''
              }`}
            />
          ))}
        </div>

        {/* Sessions Blocks */}
        {sessions.map(session => {
          const isHighlighted = !highlightPackage || session.packageName === highlightPackage;
          if (!isHighlighted) return null;

          const start = Math.max(dayStartMs, session.startTime);
          const end = Math.min(dayEndMs, session.endTime);
          if (end <= start) return null;

          const left = ((start - dayStartMs) / totalMs) * 100;
          const width = Math.max(0.6, ((end - start) / totalMs) * 100);

          return (
            <div
              key={session.id}
              onMouseEnter={() => setHoveredSession(session)}
              onMouseLeave={() => setHoveredSession(null)}
              style={{
                left: `${left}%`,
                width: `${width}%`,
                backgroundColor: session.color,
                boxShadow: session.isActiveNow ? `0 0 10px ${session.color}` : 'none'
              }}
              className={`absolute top-1 bottom-1 rounded transition-all duration-150 cursor-pointer hover:brightness-125 hover:z-20 ${
                session.isActiveNow ? 'animate-pulse' : 'opacity-90'
              }`}
            />
          );
        })}

        {/* Current Time Red Marker */}
        {currentFraction !== null && (
          <div
            style={{ left: `${currentFraction * 100}%` }}
            className="absolute top-0 bottom-0 w-0.5 bg-rose-500 z-30 pointer-events-none"
          >
            <div className="absolute -top-1 -translate-x-1/2 w-2 h-2 bg-rose-500 rounded-full shadow-lg shadow-rose-500/50" />
          </div>
        )}
      </div>

      {/* Tooltip display */}
      {hoveredSession ? (
        <div className="flex items-center justify-between text-xs py-1 px-2.5 bg-slate-900/80 border border-slate-700/60 rounded-lg text-slate-300">
          <div className="flex items-center gap-2">
            <span
              className="w-2.5 h-2.5 rounded-full"
              style={{ backgroundColor: hoveredSession.color }}
            />
            <span className="font-semibold text-white">{hoveredSession.appName}</span>
            <span className="text-slate-400">
              {formatTimeShort(hoveredSession.startTime)} →{' '}
              {hoveredSession.isActiveNow ? 'Now' : formatTimeShort(hoveredSession.endTime)}
            </span>
          </div>
          <span className="font-mono text-indigo-400 font-medium">
            {formatDuration(hoveredSession.endTime - hoveredSession.startTime)}
          </span>
        </div>
      ) : (
        /* Hour Labels (00, 06, 12, 18, 24) */
        <div className="flex justify-between text-[11px] text-slate-500 font-medium px-1">
          <span>12 AM</span>
          <span>06 AM</span>
          <span>12 PM</span>
          <span>06 PM</span>
          <span>12 AM</span>
        </div>
      )}
    </div>
  );
};
