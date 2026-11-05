package com.intervaltrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.ui.screens.AppDetailScreen
import com.intervaltrack.app.ui.screens.HomeScreen
import com.intervaltrack.app.ui.screens.PermissionScreen
import com.intervaltrack.app.ui.theme.BgDark
import com.intervaltrack.app.ui.theme.IntervalTrackTheme
import com.intervaltrack.app.ui.viewmodel.UsageViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UsageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntervalTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    var demoModeActive by remember { mutableStateOf(false) }
                    var selectedAppForDetail by remember { mutableStateOf<AppSummary?>(null) }

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
                    } else if (!uiState.hasPermission && !demoModeActive) {
                        PermissionScreen(
                            onContinueInDemo = {
                                demoModeActive = true
                            }
                        )
                    } else {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenAppDetail = { summary ->
                                selectedAppForDetail = summary
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if user granted permission while in settings
        viewModel.checkPermissionAndLoad()
    }
}
