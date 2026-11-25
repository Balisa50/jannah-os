package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: "Fajr"
        val type = intent.getStringExtra("type") ?: "adhan"
        val notificationId = 10_000 + (prayer.lowercase().hashCode() and 0x7fff)

        // Keep the next day's alarms armed as today's alarms are delivered.
        runCatching { PrayerReminderScheduler.scheduleNextDay(context) }

        when (type) {
            "adhan" -> {
                if (PrayerLock.enabled(context)) {
                    // The commitment lock is explicitly opt-in. Route through the
                    // dedicated receiver so the persisted timer and service start
                    // remain in one place.
                    context.sendBroadcast(
                        Intent(context, SalahReceiver::class.java).putExtra("prayer", prayer)
                    )
                } else {
                    vibrate(context, 10_000)
                    showNotification(
                        context,
                        "$prayer time",
                        "It is time for prayer.",
                        notificationId,
                        MainActivity::class.java,
                        notificationId
                    )
                }
            }

            "reminder" -> {
                vibrate(context, 3_000)
                showNotification(
                    context,
                    "$prayer reminder",
                    "Prepare for prayer. Iqamah is approaching.",
                    notificationId + 1,
                    MainActivity::class.java,
                    notificationId + 1
                )
            }

            "iqamah" -> {
                // Background receivers cannot reliably launch activities. Deliver a
                // high-priority notification; the user opens the alarm screen explicitly.
                showNotification(
                    context,
                    "$prayer — Iqamah",
                    "Your planned prayer time has arrived. Tap to open the prayer screen.",
                    notificationId + 2,
                    PrayerAlarmActivity::class.java,
                    notificationId + 2
                )
            }
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int,
        destination: Class<*>,
        requestCode: Int
    ) {
        val channelId = "prayer_alerts"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (destination == PrayerAlarmActivity::class.java) {
                putExtra("prayer", title.substringBefore(" —"))
                putExtra("notification_id", notificationId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setTimeoutAfter(5 * 60 * 1000L)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(notificationId, notification)
    }
}
