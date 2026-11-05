import React, { useState, useMemo } from 'react';
import { generateSessionsForDate, computeSummaries } from './mockData';
import { TimelineBar } from './components/TimelineBar';
import { SessionCard } from './components/SessionCard';
import { AppFilterChips } from './components/AppFilterChips';
import { AppDetailModal } from './components/AppDetailModal';
import { AppSummary } from './types';
import {
  Smartphone,
  Monitor,
  ChevronLeft,
  ChevronRight,
  Search,
  Clock,
  Sparkles,
  Download,
  Terminal,
  Activity
} from 'lucide-react';

export function App() {
  const [dayOffset, setDayOffset] = useState<number>(0);
  const [selectedPackage, setSelectedPackage] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [phoneFrame, setPhoneFrame] = useState<boolean>(true);
  const [detailSummary, setDetailSummary] = useState<AppSummary | null>(null);

  // Compute selected date
  const targetDate = useMemo(() => {
    const d = new Date();
    d.setDate(d.getDate() + dayOffset);
    return d;
  }, [dayOffset]);

  const isToday = dayOffset === 0;

  // Day bounds in ms
  const { dayStartMs, dayEndMs } = useMemo(() => {
    const start = new Date(targetDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(targetDate);
    end.setHours(23, 59, 59, 999);
    return { dayStartMs: start.getTime(), dayEndMs: end.getTime() };
  }, [targetDate]);

  // Generate sessions
  const allSessions = useMemo(() => {
    return generateSessionsForDate(targetDate);
  }, [targetDate]);

  const summaries = useMemo(() => {
    return computeSummaries(allSessions);
  }, [allSessions]);

  // Filtered sessions
  const filteredSessions = useMemo(() => {
    let list = allSessions;
    if (selectedPackage) {
      list = list.filter(s => s.packageName === selectedPackage);
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      list = list.filter(s => s.appName.toLowerCase().includes(q) || s.packageName.toLowerCase().includes(q));
    }
    return list;
  }, [allSessions, selectedPackage, searchQuery]);

  const activeSession = allSessions.find(s => s.isActiveNow);

  const formattedDate = useMemo(() => {
    if (dayOffset === 0) return 'Today';
    if (dayOffset === -1) return 'Yesterday';
    return targetDate.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
  }, [dayOffset, targetDate]);

  return (
    <div className="min-h-screen bg-[#05070D] text-slate-100 flex flex-col">
      {/* Top Banner & Control Bar */}
      <header className="border-b border-slate-800/80 bg-[#0B0F19]/90 backdrop-blur sticky top-0 z-40 px-4 py-3">
        <div className="max-w-5xl mx-auto flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-cyan-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <Clock className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-extrabold text-base sm:text-lg tracking-tight text-white">
                  IntervalTrack
                </h1>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                  Android Native
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">
                Monitors from which time to which time you open apps
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* Toggle Phone Frame vs Full Screen */}
            <button
              onClick={() => setPhoneFrame(!phoneFrame)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#111827] hover:bg-[#1E293B] border border-slate-800 text-xs font-medium text-slate-300 transition-colors"
            >
              {phoneFrame ? (
                <>
                  <Monitor className="w-3.5 h-3.5 text-indigo-400" />
                  <span className="hidden sm:inline">Desktop View</span>
                </>
              ) : (
                <>
                  <Smartphone className="w-3.5 h-3.5 text-indigo-400" />
                  <span className="hidden sm:inline">Phone Frame</span>
                </>
              )}
            </button>

            {/* Quick APK action badge */}
            <div className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold">
              <Download className="w-3.5 h-3.5" />
              <span>APK Ready</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 flex justify-center p-2 sm:p-6">
        <div
          className={`w-full transition-all duration-300 ${
            phoneFrame
              ? 'max-w-[430px] bg-[#090D16] border border-slate-800 rounded-[42px] p-4 shadow-2xl shadow-indigo-950/20 relative my-auto min-h-[820px] flex flex-col'
              : 'max-w-4xl bg-[#090D16] border border-slate-800/80 rounded-3xl p-6 shadow-xl flex flex-col'
          }`}
        >
          {/* Phone Speaker Notch (Phone mode only) */}
          {phoneFrame && (
            <div className="w-28 h-4 bg-slate-900 border border-slate-800 rounded-full mx-auto mb-3 flex items-center justify-center">
              <div className="w-3 h-3 rounded-full bg-slate-950 mr-2" />
              <div className="w-10 h-1 bg-slate-800 rounded-full" />
            </div>
          )}

          {/* Subheader & Date Navigation */}
          <div className="flex items-center justify-between mb-3 bg-[#111827] border border-slate-800/80 rounded-2xl p-2 px-3">
            <button
              onClick={() => setDayOffset(d => d - 1)}
              className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-white">{formattedDate}</span>
              {isToday && (
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              )}
            </div>
            <button
              onClick={() => setDayOffset(d => Math.min(0, d + 1))}
              disabled={isToday}
              className={`p-1.5 rounded-lg transition-colors ${
                isToday ? 'opacity-30 cursor-not-allowed text-slate-600' : 'hover:bg-slate-800 text-slate-400 hover:text-white'
              }`}
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {/* Active Session Notification (if currently open) */}
          {isToday && activeSession && (
            <div className="mb-3 bg-gradient-to-r from-emerald-950/60 to-slate-900 border border-emerald-500/40 rounded-2xl p-3 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="relative flex h-3 w-3">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
                </span>
                <div>
                  <div className="text-[10px] uppercase tracking-wider font-bold text-emerald-400">
                    Currently Open on Device
                  </div>
                  <div className="text-sm font-bold text-white">
                    {activeSession.appName}
                  </div>
                </div>
              </div>
              <button
                onClick={() => {
                  const s = summaries.find(x => x.packageName === activeSession.packageName);
                  if (s) setDetailSummary(s);
                }}
                className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-emerald-500/20 text-emerald-300 hover:bg-emerald-500/30 border border-emerald-500/40 transition-colors"
              >
                Inspect
              </button>
            </div>
          )}

          {/* 24-Hour Visual Gantt Timeline */}
          <div className="mb-3">
            <TimelineBar
              sessions={allSessions}
              dayStartMs={dayStartMs}
              dayEndMs={dayEndMs}
              highlightPackage={selectedPackage}
            />
          </div>

          {/* App Filter Chips */}
          <div className="mb-3">
            <AppFilterChips
              summaries={summaries}
              selectedPackage={selectedPackage}
              totalSessionsCount={allSessions.length}
              onSelect={pkg => setSelectedPackage(pkg)}
            />
          </div>

          {/* Search Box */}
          <div className="relative mb-3">
            <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Search by app name..."
              className="w-full bg-[#111827] border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          {/* Feed Header */}
          <div className="flex items-center justify-between mb-2 px-1">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              {selectedPackage
                ? `${summaries.find(s => s.packageName === selectedPackage)?.appName} Intervals`
                : 'Session Timeline'}{' '}
              <span className="text-slate-500">({filteredSessions.length} intervals)</span>
            </h2>

            {selectedPackage && (
              <button
                onClick={() => {
                  const s = summaries.find(x => x.packageName === selectedPackage);
                  if (s) setDetailSummary(s);
                }}
                className="text-xs font-semibold text-indigo-400 hover:text-indigo-300 transition-colors"
              >
                Deep Dive →
              </button>
            )}
          </div>

          {/* Chronological Session Cards Feed */}
          <div className="flex-1 overflow-y-auto space-y-2.5 pr-1">
            {filteredSessions.length === 0 ? (
              <div className="py-12 text-center text-slate-500 text-xs">
                No session intervals found.
              </div>
            ) : (
              filteredSessions.map(session => (
                <SessionCard
                  key={session.id}
                  session={session}
                  onClickApp={() => {
                    const s = summaries.find(x => x.packageName === session.packageName);
                    if (s) setDetailSummary(s);
                  }}
                />
              ))
            )}
          </div>
        </div>
      </main>

      {/* App Deep Dive Modal */}
      {detailSummary && (
        <AppDetailModal
          summary={detailSummary}
          dayStartMs={dayStartMs}
          dayEndMs={dayEndMs}
          onClose={() => setDetailSummary(null)}
        />
      )}
    </div>
  );
}
