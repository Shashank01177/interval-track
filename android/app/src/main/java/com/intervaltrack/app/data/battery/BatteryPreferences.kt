package com.intervaltrack.app.data.battery

import android.content.Context
import android.content.SharedPreferences

class BatteryPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)

    var isAlarmEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_ENABLED, value).apply()

    var targetPercentage: Int
        get() = prefs.getInt(KEY_TARGET_PERCENTAGE, 100)
        set(value) = prefs.edit().putInt(KEY_TARGET_PERCENTAGE, value.coerceIn(50, 100)).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ENABLED, value).apply()

    companion object {
        private const val KEY_IS_ENABLED = "battery_alarm_is_enabled"
        private const val KEY_TARGET_PERCENTAGE = "battery_target_percentage"
        private const val KEY_SOUND_ENABLED = "battery_sound_enabled"
        private const val KEY_VIBRATE_ENABLED = "battery_vibrate_enabled"
    }
}
