package com.intervaltrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.ui.components.AppFilterRow
import com.intervaltrack.app.ui.components.DateSelector
import com.intervaltrack.app.ui.components.SessionCard
import com.intervaltrack.app.ui.components.TimelineBar
import com.intervaltrack.app.ui.theme.AccentIndigo
import com.intervaltrack.app.ui.theme.BgDark
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextMuted
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.theme.TextSecondary
import com.intervaltrack.app.ui.viewmodel.UsageViewModel

@Composable
fun HomeScreen(
    viewModel: UsageViewModel,
    onOpenAppDetail: (AppSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "IntervalTrack",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
                Text(
                    text = "Session timeline, not just totals",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isSearchOpen = !isSearchOpen }) {
                    Icon(
                        imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                }
                IconButton(onClick = { viewModel.loadDayData(uiState.selectedDate) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Search Input (when active)
        if (isSearchOpen) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Filter by app name...", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                )
            )
        }

        // Date Picker Bar
        DateSelector(
            selectedDate = uiState.selectedDate,
            onPreviousDay = { viewModel.goToPreviousDay() },
            onNextDay = { viewModel.goToNextDay() },
            isToday = viewModel.isToday(uiState.selectedDate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // App Filter Chips
        AppFilterRow(
            summaries = uiState.appSummaries,
            selectedPackage = uiState.selectedPackage,
            totalSessionsCount = uiState.allSessions.size,
            onSelectPackage = { pkg ->
                viewModel.selectPackage(pkg)
            }
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentIndigo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Session Banner (if an app is currently open)
                uiState.activeSession?.let { active ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CURRENTLY OPEN",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = active.appName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Started at ${active.formattedStartTime}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF10B981))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Active Now",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 24-Hour Visual Gantt Bar
                item {
                    TimelineBar(
                        sessions = uiState.allSessions,
                        dayStartMs = uiState.dayStartMs,
                        dayEndMs = uiState.dayEndMs,
                        highlightPackage = uiState.selectedPackage
                    )
                }

                // Feed Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.selectedPackage != null) {
                                val currentApp = uiState.appSummaries.firstOrNull { it.packageName == uiState.selectedPackage }?.appName
                                "$currentApp Sessions (${uiState.filteredSessions.size})"
                            } else {
                                "Session Timeline (${uiState.filteredSessions.size} intervals)"
                            },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (uiState.selectedPackage != null) {
                            val summary = uiState.appSummaries.firstOrNull { it.packageName == uiState.selectedPackage }
                            if (summary != null) {
                                Text(
                                    text = "View App Deep Dive →",
                                    color = AccentIndigo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        onOpenAppDetail(summary)
                                    }
                                )
                            }
                        }
                    }
                }

                // Empty state
                if (uiState.filteredSessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No intervals recorded for this period",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Chronological Session Cards
                items(uiState.filteredSessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onClick = {
                            val summary = uiState.appSummaries.firstOrNull { it.packageName == session.packageName }
                            if (summary != null) {
                                onOpenAppDetail(summary)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
