package com.intervaltrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.ui.components.SessionCard
import com.intervaltrack.app.ui.components.TimelineBar
import com.intervaltrack.app.ui.theme.BgDark
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextMuted
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.theme.TextSecondary

@Composable
fun AppDetailScreen(
    summary: AppSummary,
    dayStartMs: Long,
    dayEndMs: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColor = try {
        Color(android.graphics.Color.parseColor(summary.colorHex))
    } catch (e: Exception) {
        Color(0xFF6366F1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(appColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = summary.appName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = summary.packageName,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Grid (2x2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Total Sessions",
                        value = "${summary.sessionCount} times",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Cumulative Time",
                        value = summary.formattedTotalDuration,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Avg Session Length",
                        value = summary.formattedAvgDuration,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Longest Session",
                        value = summary.formattedLongestDuration,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dedicated Timeline Bar for this app
            item {
                Text(
                    text = "App Usage Distribution",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                TimelineBar(
                    sessions = summary.sessions,
                    dayStartMs = dayStartMs,
                    dayEndMs = dayEndMs,
                    highlightPackage = summary.packageName
                )
            }

            // Session List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Intervals (${summary.sessions.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Earliest → Latest",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // List of Sessions
            items(summary.sessions, key = { it.id }) { session ->
                SessionCard(session = session)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
