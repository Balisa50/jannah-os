package com.jannahos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuardianService : Service() {
    private val CHANNEL_ID = "jannah_persistent_countdown"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createChannel()
        
        val notification = buildNotification("Calculating next prayer...")
        startForeground(1, notification)

        // Loop to update the persistent notification every minute
        scope.launch {
            while (isActive) {
                updateNotification()
                delay(60_000L) // Update every 1 minute
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification() {
        try {
            val lat = Settings.lat(this)
            val lon = Settings.lon(this)
            val times = SalahScheduler.todayTimes(lat, lon)
            val now = System.currentTimeMillis()
            val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
            
            val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
            
            val title: String
            val text: String
            
            if (nextKey != null) {
                val nextTime = times[nextKey]?.time ?: now
                val diff = nextTime - now
                val h = diff / 3600000
                val m = (diff / 60000) % 60
                val timeStr = if (h > 0) "${h}h ${m}m left" else "${m}m left"
                val clockStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nextTime))
                
                title = "${SalahTracker.label(nextKey).replaceFirstChar { it.uppercase() }} at $clockStr"
                text = "Next prayer in $timeStr"
            } else {
                val tomorrowFajr = (times["fajr"]?.time ?: now) + 24L * 60 * 60 * 1000
                val diff = tomorrowFajr - now
                val h = diff / 3600000
                val m = (diff / 60000) % 60
                title = "Fajr tomorrow"
                text = "Next prayer in ${h}h ${m}m"
            }

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1, buildNotification(text, title))
        } catch (e: Exception) {
            // Ignore and retry next minute
        }
    }

    private fun buildNotification(text: String, title: String = "Jannah OS"): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(android.graphics.Color.parseColor("#C2410C")) // Jannah Orange
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Silent update
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Next Prayer Countdown", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Always shows the time remaining until the next prayer."
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
