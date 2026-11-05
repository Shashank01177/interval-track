package com.intervaltrack.app.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import com.intervaltrack.app.data.battery.AlarmPlayer
import com.intervaltrack.app.data.battery.BatteryMonitorService
import com.intervaltrack.app.data.battery.BatteryPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryUiState(
    val currentLevel: Int = 85,
    val isCharging: Boolean = true,
    val targetPercentage: Int = 100,
    val isAlarmEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val isAlarmPlaying: Boolean = false
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BatteryPreferences(application.applicationContext)
    private val _uiState = MutableStateFlow(
        BatteryUiState(
            targetPercentage = prefs.targetPercentage,
            isAlarmEnabled = prefs.isAlarmEnabled,
            soundEnabled = prefs.soundEnabled,
            vibrateEnabled = prefs.vibrateEnabled
        )
    )
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                val pct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 85
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                _uiState.value = _uiState.value.copy(
                    currentLevel = pct,
                    isCharging = charging,
                    isAlarmPlaying = AlarmPlayer.isPlaying
                )
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(receiver, filter)

        // Start background monitor service if enabled
        if (prefs.isAlarmEnabled) {
            BatteryMonitorService.startService(application.applicationContext)
        }
    }

    fun setTargetPercentage(target: Int) {
        val clamped = target.coerceIn(50, 100)
        prefs.targetPercentage = clamped
        _uiState.value = _uiState.value.copy(targetPercentage = clamped)
    }

    fun toggleAlarmEnabled() {
        val newVal = !_uiState.value.isAlarmEnabled
        prefs.isAlarmEnabled = newVal
        _uiState.value = _uiState.value.copy(isAlarmEnabled = newVal)
        val ctx = getApplication<Application>().applicationContext
        if (newVal) {
            BatteryMonitorService.startService(ctx)
        } else {
            BatteryMonitorService.stopService(ctx)
            AlarmPlayer.stopAlarm()
        }
    }

    fun toggleSound() {
        val newVal = !_uiState.value.soundEnabled
        prefs.soundEnabled = newVal
        _uiState.value = _uiState.value.copy(soundEnabled = newVal)
    }

    fun toggleVibrate() {
        val newVal = !_uiState.value.vibrateEnabled
        prefs.vibrateEnabled = newVal
        _uiState.value = _uiState.value.copy(vibrateEnabled = newVal)
    }

    fun testAlarm() {
        val ctx = getApplication<Application>().applicationContext
        if (AlarmPlayer.isPlaying) {
            AlarmPlayer.stopAlarm()
            _uiState.value = _uiState.value.copy(isAlarmPlaying = false)
        } else {
            AlarmPlayer.startAlarm(ctx, _uiState.value.soundEnabled, _uiState.value.vibrateEnabled)
            _uiState.value = _uiState.value.copy(isAlarmPlaying = true)
        }
    }

    fun stopAlarm() {
        AlarmPlayer.stopAlarm()
        _uiState.value = _uiState.value.copy(isAlarmPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(receiver)
        } catch (e: Exception) {
            // ignore
        }
    }
}
