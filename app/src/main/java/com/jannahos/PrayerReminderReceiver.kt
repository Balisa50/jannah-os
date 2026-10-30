package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: "Fajr"

        // 1. Create the Full-Screen Intent
        val fullScreenIntent = Intent(context, PrayerAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("prayer", prayer)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Create the High-Priority Notification Channel
        val channelId = "prayer_alarm_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Prayer Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarms for prayer times"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setBypassDnd(true)
            }
            nm.createNotificationChannel(channel)
        }

        // 3. Build the Notification (Acts like a phone call)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$prayer Time")
            .setContentText("It is time to pray.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // THIS FORCES THE SCREEN ON
            .setOngoing(true)
            .build()

        // 4. Fire it
        nm.notify(1001, notification)
        
        // 5. Also try to start the activity directly (fallback)
        try { context.startActivity(fullScreenIntent) } catch (_: Exception) {}
    }
}
