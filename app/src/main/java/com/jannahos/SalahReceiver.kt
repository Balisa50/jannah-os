package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class SalahReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return

        if (prayer == "reschedule") {
            // Re-arm tomorrow's prayers using default coordinates.
            SalahScheduler.schedule(context, 13.4549, -16.5790)
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "salah_time"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(
                    channelId,
                    "Prayer time",
                    NotificationManager.IMPORTANCE_HIGH
                )
                ch.description = "Alert when it is time for prayer"
                nm.createNotificationChannel(ch)
            }
        }

        val label = SalahTracker.label(prayer)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("It is time for $label")
            .setContentText("Make wudu and pray.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        nm.notify(prayer.hashCode(), notification)
    }
}
