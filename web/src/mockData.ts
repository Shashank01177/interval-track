import { AppSession, AppSummary } from './types';

export function generateSessionsForDate(targetDate: Date): AppSession[] {
  const base = new Date(targetDate);
  base.setHours(0, 0, 0, 0);

  const t = (h: number, m: number) => {
    const d = new Date(base);
    d.setHours(h, m, 0, 0);
    return d.getTime();
  };

  const isToday = new Date().toDateString() === targetDate.toDateString();
  const now = Date.now();

  const sessions: AppSession[] = [
    {
      id: '1',
      packageName: 'com.google.android.youtube',
      appName: 'YouTube',
      startTime: t(21, 15),
      endTime: t(22, 10),
      color: '#EF4444'
    },
    {
      id: '2',
      packageName: 'com.instagram.android',
      appName: 'Instagram',
      startTime: t(20, 25),
      endTime: t(20, 48),
      color: '#EC4899'
    },
    {
      id: '3',
      packageName: 'com.whatsapp',
      appName: 'WhatsApp',
      startTime: t(19, 50),
      endTime: t(20, 2),
      color: '#10B981'
    },
    {
      id: '4',
      packageName: 'com.android.chrome',
      appName: 'Chrome',
      startTime: t(18, 10),
      endTime: t(18, 52),
      color: '#3B82F6'
    },
    {
      id: '5',
      packageName: 'com.spotify.music',
      appName: 'Spotify',
      startTime: t(16, 30),
      endTime: t(17, 30),
      color: '#22C55E'
    },
    {
      id: '6',
      packageName: 'com.slack',
      appName: 'Slack',
      startTime: t(15, 10),
      endTime: t(15, 38),
      color: '#8B5CF6'
    },
    {
      id: '7',
      packageName: 'com.instagram.android',
      appName: 'Instagram',
      startTime: t(14, 0),
      endTime: t(14, 19),
      color: '#EC4899'
    },
    {
      id: '8',
      packageName: 'com.whatsapp',
      appName: 'WhatsApp',
      startTime: t(13, 20),
      endTime: t(13, 35),
      color: '#10B981'
    },
    {
      id: '9',
      packageName: 'com.google.android.youtube',
      appName: 'YouTube',
      startTime: t(12, 10),
      endTime: t(12, 55),
      color: '#EF4444'
    },
    {
      id: '10',
      packageName: 'com.android.chrome',
      appName: 'Chrome',
      startTime: t(11, 20),
      endTime: t(11, 45),
      color: '#3B82F6'
    },
    {
      id: '11',
      packageName: 'com.slack',
      appName: 'Slack',
      startTime: t(10, 0),
      endTime: t(10, 45),
      color: '#8B5CF6'
    },
    {
      id: '12',
      packageName: 'com.whatsapp',
      appName: 'WhatsApp',
      startTime: t(9, 15),
      endTime: t(9, 28),
      color: '#10B981'
    },
    {
      id: '13',
      packageName: 'com.spotify.music',
      appName: 'Spotify',
      startTime: t(8, 30),
      endTime: t(9, 10),
      color: '#22C55E'
    }
  ];

  if (isToday) {
    // Add current live active session
    sessions.unshift({
      id: 'live-0',
      packageName: 'com.instagram.android',
      appName: 'Instagram',
      startTime: now - 14 * 60 * 1000,
      endTime: now,
      color: '#EC4899',
      isActiveNow: true
    });
  }

  // Calculate gaps between consecutive sessions for the same app
  const chronological = [...sessions].sort((a, b) => a.startTime - b.startTime);
  const lastCloseMap: Record<string, number> = {};

  const withGaps = chronological.map(session => {
    const prevClose = lastCloseMap[session.packageName];
    const gap = prevClose && session.startTime > prevClose ? session.startTime - prevClose : 0;
    lastCloseMap[session.packageName] = session.endTime;
    return { ...session, gapSincePreviousMs: gap };
  });

  return withGaps.sort((a, b) => b.startTime - a.startTime);
}

export function computeSummaries(sessions: AppSession[]): AppSummary[] {
  const map: Record<string, AppSession[]> = {};
  sessions.forEach(s => {
    if (!map[s.packageName]) map[s.packageName] = [];
    map[s.packageName].push(s);
  });

  return Object.entries(map).map(([pkg, appSessions]) => {
    const totalDurationMs = appSessions.reduce((acc, s) => acc + (s.endTime - s.startTime), 0);
    const count = appSessions.length;
    const avgDurationMs = count > 0 ? totalDurationMs / count : 0;
    const longestDurationMs = Math.max(...appSessions.map(s => s.endTime - s.startTime));

    return {
      packageName: pkg,
      appName: appSessions[0].appName,
      color: appSessions[0].color,
      sessionCount: count,
      totalDurationMs,
      avgDurationMs,
      longestDurationMs,
      sessions: [...appSessions].sort((a, b) => b.startTime - a.startTime)
    };
  }).sort((a, b) => b.sessionCount - a.sessionCount);
}

export function formatDuration(ms: number): string {
  const totalSeconds = Math.max(1, Math.floor(ms / 1000));
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) return `${hours}h ${minutes}m`;
  if (minutes > 0) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}

export function formatTime(ms: number): string {
  const d = new Date(ms);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true });
}

export function formatTimeShort(ms: number): string {
  const d = new Date(ms);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });
}
