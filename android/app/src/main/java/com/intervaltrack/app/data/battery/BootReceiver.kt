package com.intervaltrack.app.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = BatteryPreferences(context)
        if (prefs.isAlarmEnabled) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_POWER_CONNECTED -> {
                    BatteryMonitorService.startService(context)
                }
            }
        }
    }
}
