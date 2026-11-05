package com.intervaltrack.app.data.battery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.intervaltrack.app.MainActivity

class BatteryMonitorService : Service() {

    private lateinit var prefs: BatteryPreferences
    private var alarmAlreadyTriggered = false
    private var currentLevel = -1
    private var isCharging = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                    if (level != -1 && scale != -1) {
                        currentLevel = (level * 100 / scale.toFloat()).toInt()
                    }

                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    checkBatteryAndAlert()
                }

                Intent.ACTION_POWER_DISCONNECTED -> {
                    // Charger unplugged: stop alarm and reset trigger state
                    isCharging = false
                    alarmAlreadyTriggered = false
                    AlarmPlayer.stopAlarm()
                    updateNotification(isAlarming = false)
                }

                Intent.ACTION_POWER_CONNECTED -> {
                    alarmAlreadyTriggered = false
                    isCharging = true
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = BatteryPreferences(this)
        createNotificationChannels()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)

        startForeground(NOTIFICATION_ID, buildMonitoringNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            AlarmPlayer.stopAlarm()
            alarmAlreadyTriggered = true
            updateNotification(isAlarming = false)
        } else if (intent?.action == ACTION_TEST_ALARM) {
            AlarmPlayer.startAlarm(this, prefs.soundEnabled, prefs.vibrateEnabled)
            updateNotification(isAlarming = true)
        }

        return START_STICKY
    }

    private fun checkBatteryAndAlert() {
        if (!prefs.isAlarmEnabled) return

        val target = prefs.targetPercentage

        if (isCharging && currentLevel >= target) {
            if (!alarmAlreadyTriggered && !AlarmPlayer.isPlaying) {
                alarmAlreadyTriggered = true
                // Play alarm sound and vibrate!
                AlarmPlayer.startAlarm(this, prefs.soundEnabled, prefs.vibrateEnabled)
                updateNotification(isAlarming = true)
            }
        } else {
            // Below target or unplugged
            if (currentLevel < target) {
                alarmAlreadyTriggered = false
            }
            updateNotification(isAlarming = false)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Silent monitoring channel
            val monitorChannel = NotificationChannel(
                CHANNEL_ID_MONITOR,
                "Battery Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors battery level while charging"
            }

            // High-priority Heads-up alarm channel
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERT,
                "Battery Full / Target Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sounds alarm when battery reaches target percentage"
                enableVibration(true)
            }

            manager?.createNotificationChannel(monitorChannel)
            manager?.createNotificationChannel(alertChannel)
        }
    }

    private fun buildMonitoringNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val target = prefs.targetPercentage
        val statusText = if (isCharging) {
            "Charging: $currentLevel% · Target: $target%"
        } else {
            "Battery: $currentLevel% · Alarm active on plug-in"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_MONITOR)
            .setContentTitle("Battery Charge Alarm")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(isAlarming: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (isAlarming) {
            val stopIntent = Intent(this, BatteryMonitorService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val target = prefs.targetPercentage
            val alertNotif = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
                .setContentTitle("⚡ Battery Target Reached: $currentLevel%!")
                .setContentText("Your phone has reached $target%. Unplug charger now.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS ALARM", stopPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            manager.notify(NOTIFICATION_ID, alertNotif)
        } else {
            manager.notify(NOTIFICATION_ID, buildMonitoringNotification())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        AlarmPlayer.stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID_MONITOR = "battery_monitor_channel"
        const val CHANNEL_ID_ALERT = "battery_alert_channel"
        const val ACTION_STOP_ALARM = "com.intervaltrack.app.ACTION_STOP_ALARM"
        const val ACTION_TEST_ALARM = "com.intervaltrack.app.ACTION_TEST_ALARM"

        fun startService(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
