package com.intervaltrack.app.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.intervaltrack.app.data.model.AppSession
import com.intervaltrack.app.data.model.AppSummary
import java.util.Calendar
import java.util.UUID

class UsageStatsHelper(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val usageStatsManager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    private val appLabelCache = mutableMapOf<String, String>()

    /**
     * Checks if the app has been granted PACKAGE_USAGE_STATS permission
     */
    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Creates an intent to navigate directly to the system Usage Access settings
     */
    fun getUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Queries and computes exact session intervals (from what time to what time)
     * for a given start and end timestamp.
     */
    fun queryAppSessions(
        startTime: Long,
        endTime: Long,
        excludeSystemLaunchers: Boolean = true
    ): List<AppSession> {
        if (!hasUsagePermission() || usageStatsManager == null) {
            return getMockSessionsIfEmpty(startTime, endTime)
        }

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        // Track open start times per package
        val activeStarts = mutableMapOf<String, Long>()
        val rawSessions = mutableListOf<AppSession>()

        // System packages to optionally ignore
        val ignoredPackages = setOf(
            "android",
            "com.android.systemui",
            context.packageName
        )

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val time = event.timeStamp

            if (excludeSystemLaunchers && ignoredPackages.contains(pkg)) {
                continue
            }

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // App brought to foreground (start of session)
                    activeStarts[pkg] = time
                }

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    // App left foreground (end of session)
                    val sessionStart = activeStarts.remove(pkg)
                    if (sessionStart != null && sessionStart in startTime..endTime) {
                        val sessionEnd = time.coerceAtMost(endTime)
                        val duration = sessionEnd - sessionStart

                        // Only consider sessions of at least 1.5 seconds to filter out transition glitches
                        if (duration >= 1500L) {
                            rawSessions.add(
                                AppSession(
                                    id = UUID.randomUUID().toString(),
                                    packageName = pkg,
                                    appName = getAppName(pkg),
                                    startTime = sessionStart,
                                    endTime = sessionEnd,
                                    isActiveNow = false
                                )
                            )
                        }
                    }
                }
            }
        }

        // If today and an app is still open in foreground right now
        val now = System.currentTimeMillis()
        if (endTime >= now - 60_000) {
            for ((pkg, start) in activeStarts) {
                if (start in startTime..now && (now - start) >= 1500L) {
                    rawSessions.add(
                        AppSession(
                            id = UUID.randomUUID().toString(),
                            packageName = pkg,
                            appName = getAppName(pkg),
                            startTime = start,
                            endTime = now,
                            isActiveNow = true
                        )
                    )
                }
            }
        }

        if (rawSessions.isEmpty()) {
            return getMockSessionsIfEmpty(startTime, endTime)
        }

        // Sort descending by start time (newest session first)
        val sorted = rawSessions.sortedByDescending { it.startTime }

        // Compute gaps between consecutive sessions for the same app
        return calculateGaps(sorted)
    }

    private fun calculateGaps(sessions: List<AppSession>): List<AppSession> {
        val appLastCloseTime = mutableMapOf<String, Long>()
        // Process chronologically (earliest first) to compute gaps correctly
        val chronological = sessions.sortedBy { it.startTime }
        val result = mutableListOf<AppSession>()

        for (session in chronological) {
            val lastClose = appLastCloseTime[session.packageName]
            val gap = if (lastClose != null && session.startTime > lastClose) {
                session.startTime - lastClose
            } else 0L

            result.add(session.copy(gapSincePreviousMs = gap))
            appLastCloseTime[session.packageName] = session.endTime
        }

        // Return sorted newest first
        return result.sortedByDescending { it.startTime }
    }

    /**
     * Groups sessions into AppSummary objects per package
     */
    fun computeAppSummaries(sessions: List<AppSession>): List<AppSummary> {
        val grouped = sessions.groupBy { it.packageName }
        return grouped.map { (pkg, appSessions) ->
            val totalDur = appSessions.sumOf { it.durationMs }
            val count = appSessions.size
            val avgDur = if (count > 0) totalDur / count else 0L
            val longestDur = appSessions.maxOfOrNull { it.durationMs } ?: 0L
            val firstStart = appSessions.minOfOrNull { it.startTime } ?: 0L
            val lastEnd = appSessions.maxOfOrNull { it.endTime } ?: 0L

            AppSummary(
                packageName = pkg,
                appName = appSessions.firstOrNull()?.appName ?: pkg,
                colorHex = appSessions.firstOrNull()?.colorHex ?: "#6366F1",
                sessionCount = count,
                totalDurationMs = totalDur,
                averageSessionDurationMs = avgDur,
                longestSessionDurationMs = longestDur,
                firstStartTime = firstStart,
                lastEndTime = lastEnd,
                sessions = appSessions.sortedByDescending { it.startTime }
            )
        }.sortedByDescending { it.sessionCount } // Sorted by most active / session count
    }

    fun getAppName(packageName: String): String {
        return appLabelCache.getOrPut(packageName) {
            try {
                val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(packageName, 0)
                }
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                // Return cleaned package name if not found
                packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            }
        }
    }

    /**
     * Realistic sample intervals to preview the UI immediately if running without permission or in preview
     */
    fun getMockSessionsIfEmpty(startTime: Long, endTime: Long): List<AppSession> {
        val baseCalendar = Calendar.getInstance().apply {
            timeInMillis = startTime
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val t8_00 = baseCalendar.timeInMillis

        fun timeAt(hours: Int, mins: Int): Long {
            val c = Calendar.getInstance().apply {
                timeInMillis = startTime
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, mins)
                set(Calendar.SECOND, 0)
            }
            return c.timeInMillis
        }

        val mockList = listOf(
            AppSession(UUID.randomUUID().toString(), "com.google.android.youtube", "YouTube", timeAt(21, 15), timeAt(22, 10)),
            AppSession(UUID.randomUUID().toString(), "com.instagram.android", "Instagram", timeAt(20, 30), timeAt(20, 48)),
            AppSession(UUID.randomUUID().toString(), "com.whatsapp", "WhatsApp", timeAt(19, 50), timeAt(19, 58)),
            AppSession(UUID.randomUUID().toString(), "com.android.chrome", "Chrome", timeAt(18, 10), timeAt(18, 45)),
            AppSession(UUID.randomUUID().toString(), "com.spotify.music", "Spotify", timeAt(16, 30), timeAt(17, 45)),
            AppSession(UUID.randomUUID().toString(), "com.slack", "Slack", timeAt(15, 10), timeAt(15, 35)),
            AppSession(UUID.randomUUID().toString(), "com.instagram.android", "Instagram", timeAt(14, 0), timeAt(14, 18)),
            AppSession(UUID.randomUUID().toString(), "com.whatsapp", "WhatsApp", timeAt(13, 30), timeAt(13, 36)),
            AppSession(UUID.randomUUID().toString(), "com.google.android.youtube", "YouTube", timeAt(12, 15), timeAt(12, 55)),
            AppSession(UUID.randomUUID().toString(), "com.android.chrome", "Chrome", timeAt(11, 20), timeAt(11, 45)),
            AppSession(UUID.randomUUID().toString(), "com.slack", "Slack", timeAt(10, 0), timeAt(10, 42)),
            AppSession(UUID.randomUUID().toString(), "com.whatsapp", "WhatsApp", timeAt(9, 15), timeAt(9, 22)),
            AppSession(UUID.randomUUID().toString(), "com.spotify.music", "Spotify", timeAt(8, 30), timeAt(9, 0))
        ).filter { it.startTime in startTime..endTime }

        return calculateGaps(mockList)
    }
}
