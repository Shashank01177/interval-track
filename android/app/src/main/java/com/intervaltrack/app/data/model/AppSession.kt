package com.intervaltrack.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppSession(
    val id: String,
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val isActiveNow: Boolean = false,
    val colorHex: String = getColorForPackage(packageName),
    val gapSincePreviousMs: Long = 0L
) {
    val durationMs: Long
        get() = (endTime - startTime).coerceAtLeast(1000L)

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

    val formattedStartTime: String
        get() = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(startTime))

    val formattedEndTime: String
        get() = if (isActiveNow) "In Progress" else SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(endTime))

    val formattedTimeRange: String
        get() = if (isActiveNow) {
            "${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(startTime))} – Now"
        } else {
            "${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(startTime))} – ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(endTime))}"
        }

    val formattedGap: String?
        get() {
            if (gapSincePreviousMs <= 0) return null
            val mins = gapSincePreviousMs / (60 * 1000)
            val hours = mins / 60
            return when {
                hours > 0 -> "${hours}h ${mins % 60}m gap"
                mins > 0 -> "${mins}m gap"
                else -> "${gapSincePreviousMs / 1000}s gap"
            }
        }

    companion object {
        fun getColorForPackage(pkg: String): String {
            val colors = listOf(
                "#6366F1", // Indigo
                "#EC4899", // Pink
                "#10B981", // Emerald
                "#F59E0B", // Amber
                "#8B5CF6", // Purple
                "#06B6D4", // Cyan
                "#EF4444", // Red
                "#14B8A6", // Teal
                "#3B82F6", // Blue
                "#F97316"  // Orange
            )
            val hash = kotlin.math.abs(pkg.hashCode())
            return colors[hash % colors.size]
        }
    }
}
