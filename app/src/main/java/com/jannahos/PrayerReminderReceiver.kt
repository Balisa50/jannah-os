package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val times = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
        val now = System.currentTimeMillis()
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        
        val nextKey = ordered.firstOrNull { (times[it] ?: 0L) > now }
        
        if (nextKey != null) {
            val nextTime = times[nextKey] ?: now
            val diff = nextTime - now
            val h = diff / 3600000
            val m = (diff / 60000) % 60
            val timeStr = if (h > 0) "${h}h ${m}m left" else "${m}m left"
            val clockStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nextTime))
            val name = SalahTracker.label(nextKey).uppercase()

            showNotification(ctx, name, clockStr, timeStr)
        }
        
        // Schedule the next one in the adaptive chain
        PrayerReminderScheduler.scheduleNextReminder(ctx)
    }

    private fun showNotification(ctx: Context, prayer: String, clock: String, timeLeft: String) {
        val channelId = "jannah_adaptive_prayer"
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Prayer Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 10000)
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(ctx, channelId)
            .setContentTitle("$prayer at $clock")
            .setContentText(timeLeft)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVibrate(longArrayOf(0, 10000))
            .setTimeoutAfter(30_000) // Auto-dismisses after 30 seconds
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(100, notif)
    }
}
