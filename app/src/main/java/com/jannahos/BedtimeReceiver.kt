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
        // A lock is armed only when its notification can be posted, because
        // the notification is the supported entry point into the lock screen.
        // The recurring bedtime alarm is re-armed independently below.
        runCatching { activate(context, 8 * 60 * 60 * 1000L) }
        runCatching { BedtimeScheduler.schedule(context) }
    }

    companion object {
        private const val CHANNEL_ID = "bedtime_lock"
        private const val NOTIFICATION_ID = 7788

        fun activate(context: Context, durationMs: Long) {
            if (!NotificationAccess.canPost(context)) return
            BedtimeStore.arm(context, durationMs)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Bedtime lock",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Opens your bedtime reading lock when tapped"
                        setSound(null, null)
                    }
                )
            }

            val openIntent = Intent(context, BedtimeLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Bedtime reading")
                .setContentText("Your bedtime lock is ready. Tap to open Surah Al-Mulk.")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            runCatching { manager.notify(NOTIFICATION_ID, notification) }
                .onFailure { BedtimeStore.release(context) }
        }
    }
}
