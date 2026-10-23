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
        val name = SalahTracker.label(prayer)

        // 1. Start Adhan Sound
        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            putExtra("prayer", prayer)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Trigger Full Screen Prayer Lock
        val channelId = "prayer_lock_full"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Prayer Lock", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, SalahCommitmentActivity::class.java).apply {
            putExtra("prayer", prayer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("$name Time")
            .setContentText("Put the phone down. Go pray.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // THIS FORCES THE SCREEN
            .setAutoCancel(true)
            .build()

        nm.notify(prayer.hashCode(), notif)
    }
}
