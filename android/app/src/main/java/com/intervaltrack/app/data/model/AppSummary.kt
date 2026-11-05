package com.intervaltrack.app.data.model

data class AppSummary(
    val packageName: String,
    val appName: String,
    val colorHex: String,
    val sessionCount: Int,
    val totalDurationMs: Long,
    val averageSessionDurationMs: Long,
    val longestSessionDurationMs: Long,
    val firstStartTime: Long,
    val lastEndTime: Long,
    val sessions: List<AppSession>
) {
    val formattedTotalDuration: String
        get() {
            val totalSeconds = totalDurationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${totalSeconds}s"
            }
        }

    val formattedAvgDuration: String
        get() {
            val mins = (averageSessionDurationMs / 1000) / 60
            val secs = (averageSessionDurationMs / 1000) % 60
            return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }

    val formattedLongestDuration: String
        get() {
            val mins = (longestSessionDurationMs / 1000) / 60
            val secs = (longestSessionDurationMs / 1000) % 60
            return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }
}
