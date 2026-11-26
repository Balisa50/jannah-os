package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class FridayKahfReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val index = intent.getIntExtra("reminder_index", 0)
        // Re-arm only this reminder; the other Friday check-ins must remain armed.
        runCatching { FridayKahfScheduler.scheduleNext(ctx, index) }
        showNotification(ctx, index)
    }

    private fun showNotification(ctx: Context, index: Int) {
        if (!NotificationAccess.canPost(ctx)) return
        val channelId = "friday_kahf_channel"
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Friday Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders to read Surah Al-Kahf on Fridays"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pendingIntent = PendingIntent.getActivity(ctx, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Change the message slightly to keep it fresh
        val message = when (index) {
            0 -> "Start your Friday with Surah Al-Kahf. It is a light for the week."
            1 -> "Have you read Surah Al-Kahf yet? Take 10 minutes."
            2 -> "Midday reminder: Recite Surah Al-Kahf for protection."
            3 -> "Afternoon check-in: Don't forget Surah Al-Kahf."
            else -> "Last reminder for today: Read Surah Al-Kahf before the day ends."
        }

        val notif = NotificationCompat.Builder(ctx, channelId)
            .setContentTitle("Friday Kahf")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { nm.notify(8000 + index, notif) }
    }
}
