package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PrePrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "pre_prayer"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(chId) == null) {
                val ch = NotificationChannel(
                    chId,
                    "Pre-prayer warning",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                ch.description = "A gentle heads-up 15 minutes before prayer."
                nm.createNotificationChannel(ch)
            }
        }

        val launch = Intent(context, SalahCommitmentActivity::class.java).apply {
            putExtra("prayer", prayer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getActivity(context, prayer.hashCode() + 7, launch, flags)

        val label = SalahTracker.label(prayer)
        val notif = NotificationCompat.Builder(context, chId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$label in 15 minutes")
            .setContentText("Finish what you are doing. Prepare for prayer.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(prayer.hashCode() + 900, notif)
    }
}
