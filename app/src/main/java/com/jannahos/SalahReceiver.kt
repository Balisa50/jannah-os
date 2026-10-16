package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class SalahReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return

        if (prayer == "reschedule") {
            SalahScheduler.schedule(context, Settings.lat(context), Settings.lon(context))
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "salah_time"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                val ch = NotificationChannel(
                    channelId,
                    "Prayer time",
                    NotificationManager.IMPORTANCE_HIGH
                )
                ch.description = "Alert when it is time for prayer"
                nm.createNotificationChannel(ch)
            }
        }

        val launchIntent = Intent(context, SalahCommitmentActivity::class.java).apply {
            putExtra("prayer", prayer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        val contentPi = PendingIntent.getActivity(context, prayer.hashCode() + 1, launchIntent, flags)

        val label = SalahTracker.label(prayer)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("It is time for $label")
            .setContentText("Tap to open the prayer window.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPi)
            .setAutoCancel(true)
            .build()

        nm.notify(prayer.hashCode(), notification)
    }
}
