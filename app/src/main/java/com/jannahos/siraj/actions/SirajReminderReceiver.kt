package com.jannahos.siraj.actions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jannahos.MainActivity
import com.jannahos.R

class SirajReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE)?.trim().orEmpty()
        if (title.isBlank()) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "SIRAJ reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders explicitly scheduled by you through SIRAJ"
                }
            )
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("A reminder you asked SIRAJ to schedule.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        val notificationId = intent.dataString?.hashCode() ?: title.hashCode()
        manager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_TITLE = "siraj_reminder_title"
        private const val CHANNEL_ID = "siraj_user_reminders"
    }
}
