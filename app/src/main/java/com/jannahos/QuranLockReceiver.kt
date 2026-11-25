package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class QuranLockReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (!QuranReadingLock.enabled(ctx)) return
        if (!NotificationAccess.canPost(ctx)) {
            runCatching { QuranLockScheduler.scheduleNextDay(ctx) }
            return
        }
        if (QuranReadingLock.isDoneToday(ctx)) {
            // Completing today's reading before the random alarm must not
            // silently disable the feature for every future day.
            runCatching { QuranLockScheduler.scheduleNextDay(ctx) }
            return
        }
        QuranReadingLock.setActiveToday(ctx, true)
        
        val channelId = "quran_lock_full"
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Quran Lock", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(ctx, MainActivity::class.java).apply {
            putExtra("open_quran_lock", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(ctx, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notif = NotificationCompat.Builder(ctx, channelId)
            .setContentTitle("Quran Lock")
            .setContentText("It is time to read your daily page.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching { nm.notify(777, notif) }
            .onFailure { QuranReadingLock.setActiveToday(ctx, false) }
        runCatching { QuranLockScheduler.scheduleNextDay(ctx) }
    }
}
