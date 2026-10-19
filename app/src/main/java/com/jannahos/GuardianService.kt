package com.jannahos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GuardianService : Service() {
    private val CHANNEL_ID = "jannah_guardian_channel"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
        
        // Loop to update prayer countdown every minute
        scope.launch {
            while (isActive) {
                updatePrayerNotification()
                delay(60000) // 1 minute
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updatePrayerNotification() {
        try {
            val times = SalahScheduler.todayTimes(Settings.lat(this), Settings.lon(this))
            val now = System.currentTimeMillis()
            val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
            
            val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
            if (nextKey != null) {
                val nextTime = times[nextKey]?.time ?: now
                val diff = nextTime - now
                val h = diff / 3600000
                val m = (diff / 60000) % 60
                val timeString = String.format("%02d:%02d", h, m)
                val name = SalahTracker.label(nextKey).uppercase()
                
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(1, buildNotification("Next: $name in $timeString"))
            }
        } catch (_: Exception) {}
    }

    private fun buildNotification(text: String = "Protecting your focus and prayer times."): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jannah OS")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Silent update
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Prayer Countdown", NotificationManager.IMPORTANCE_LOW)
            channel.setSound(null, null)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
