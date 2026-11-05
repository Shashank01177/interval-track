export interface AppSession {
  id: string;
  packageName: string;
  appName: string;
  startTime: number; // epoch ms
  endTime: number; // epoch ms
  color: string;
  isActiveNow?: boolean;
  gapSincePreviousMs?: number;
}

export interface AppSummary {
  packageName: string;
  appName: string;
  color: string;
  sessionCount: number;
  totalDurationMs: number;
  avgDurationMs: number;
  longestDurationMs: number;
  sessions: AppSession[];
}
