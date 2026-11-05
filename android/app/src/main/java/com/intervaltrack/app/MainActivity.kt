package com.intervaltrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.ui.screens.AppDetailScreen
import com.intervaltrack.app.ui.screens.BatteryScreen
import com.intervaltrack.app.ui.screens.HomeScreen
import com.intervaltrack.app.ui.screens.PermissionScreen
import com.intervaltrack.app.ui.theme.AccentIndigo
import com.intervaltrack.app.ui.theme.BgDark
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.IntervalTrackTheme
import com.intervaltrack.app.ui.theme.TextMuted
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.viewmodel.BatteryViewModel
import com.intervaltrack.app.ui.viewmodel.UsageViewModel

enum class MainTab {
    TIMELINE,
    BATTERY
}

class MainActivity : ComponentActivity() {

    private val usageViewModel: UsageViewModel by viewModels()
    private val batteryViewModel: BatteryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntervalTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    val uiState by usageViewModel.uiState.collectAsState()
                    var demoModeActive by remember { mutableStateOf(false) }
                    var selectedAppForDetail by remember { mutableStateOf<AppSummary?>(null) }
                    var currentTab by remember { mutableStateOf(MainTab.TIMELINE) }

                    // Navigation handling
                    if (selectedAppForDetail != null) {
                        BackHandler {
                            selectedAppForDetail = null
                        }
                        AppDetailScreen(
                            summary = selectedAppForDetail!!,
                            dayStartMs = uiState.dayStartMs,
                            dayEndMs = uiState.dayEndMs,
                            onBack = { selectedAppForDetail = null }
                        )
                    } else if (!uiState.hasPermission && !demoModeActive && currentTab == MainTab.TIMELINE) {
                        PermissionScreen(
                            onContinueInDemo = {
                                demoModeActive = true
                            }
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Active Screen Content
                            Box(modifier = Modifier.weight(1f)) {
                                when (currentTab) {
                                    MainTab.TIMELINE -> {
                                        HomeScreen(
                                            viewModel = usageViewModel,
                                            onOpenAppDetail = { summary ->
                                                selectedAppForDetail = summary
                                            }
                                        )
                                    }
                                    MainTab.BATTERY -> {
                                        BatteryScreen(viewModel = batteryViewModel)
                                    }
                                }
                            }

                            // Bottom Navigation Bar
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .border(1.dp, BorderDark),
                                containerColor = CardDark,
                                contentColor = TextPrimary
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == MainTab.TIMELINE,
                                    onClick = { currentTab = MainTab.TIMELINE },
                                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Intervals") },
                                    label = { Text("App Intervals", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentIndigo,
                                        selectedTextColor = AccentIndigo,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color(0xFF0F172A)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainTab.BATTERY,
                                    onClick = { currentTab = MainTab.BATTERY },
                                    icon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery Alarm") },
                                    label = { Text("Charge Alarm", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF10B981),
                                        selectedTextColor = Color(0xFF10B981),
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color(0xFF0F172A)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        usageViewModel.checkPermissionAndLoad()
    }
}
