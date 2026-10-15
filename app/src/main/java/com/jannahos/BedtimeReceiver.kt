package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bedtime"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                val ch = NotificationChannel(
                    channelId, "Bedtime", NotificationManager.IMPORTANCE_DEFAULT
                )
                ch.description = "Gentle reminder before your intended bedtime"
                nm.createNotificationChannel(ch)
            }
        }

        val onTonight = TahajjudPlan.isOnTonight(context)
        val body = if (onTonight)
            "Tonight is a tahajjud night. Sleep soon."
        else
            "Wind down. Return to sleep on time."

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Bedtime in 30 minutes")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(6201, notif)

        // Re-arm for tomorrow.
        BedtimeScheduler.schedule(context)
    }
}
