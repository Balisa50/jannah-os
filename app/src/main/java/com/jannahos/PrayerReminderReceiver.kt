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
        val minutes = intent.getIntExtra("minutes", 0)

        // 1. Vibrate for 30 seconds
        vibrateReminder(context)

        // 2. Show Dismissible Notification
        val channelId = "prayer_reminder_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Prayer Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders before prayer"
                enableVibration(false) // We handle vibration manually
            }
            nm.createNotificationChannel(channel)
        }

        // Open app when tapped
        val openIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val title = if (minutes > 0) "$prayer in $minutes minutes" else "$prayer Time"
        val body = if (minutes > 0) "Prepare for prayer." else "It is time to pray."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true) // Dismisses when tapped
            .setOngoing(false)   // Allows swiping away
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(prayer.hashCode() + minutes, notification)
    }

    private fun vibrateReminder(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Vibrate for 30 seconds (30000 milliseconds)
            vibrator.vibrate(VibrationEffect.createOneShot(30000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30000)
        }
    }
}
