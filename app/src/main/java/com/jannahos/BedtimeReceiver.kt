package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Settings.isBedtimeLockEnabled(context)) return

        val channelId = "bedtime_lock_full"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Bedtime Lock", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, BedtimeLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Bedtime")
            .setContentText("Time to read Surah Al-Mulk.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        nm.notify(888, notif)
    }
}
