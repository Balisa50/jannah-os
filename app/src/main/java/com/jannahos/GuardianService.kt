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

class GuardianService : Service() {
    private val CHANNEL_SILENT = "jannah_guardian_silent"
    private val CHANNEL_BREATHING = "jannah_guardian_breathing"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createChannels()
        startForeground(1, buildSilentNotification())
        
        // Loop to trigger the "breathing" prayer notifications
        scope.launch {
            while (isActive) {
                try {
                    val times = SalahScheduler.todayTimes(Settings.lat(this@GuardianService), Settings.lon(this@GuardianService))
                    val now = System.currentTimeMillis()
                    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
                    
                    val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
                    if (nextKey != null) {
                        val nextTime = times[nextKey]?.time ?: now
                        val diff = nextTime - now
                        val name = SalahTracker.label(nextKey).uppercase()
                        
                        // Trigger the heads-up notification
                        sendBreathingNotification(name, nextTime, diff)
                        
                        // Calculate when to pop up next based on how close the prayer is
                        val waitMs = when {
                            diff > 3 * 60 * 60 * 1000L -> 60 * 60 * 1000L // > 3 hours: every hour
                            diff > 60 * 60 * 1000L -> 30 * 60 * 1000L     // > 1 hour: every 30 mins
                            diff > 30 * 60 * 1000L -> 15 * 60 * 1000L     // > 30 mins: every 15 mins
                            diff > 10 * 60 * 1000L -> 5 * 60 * 1000L      // > 10 mins: every 5 mins
                            else -> 2 * 60 * 1000L                        // < 10 mins: every 2 mins
                        }
                        delay(waitMs)
                    } else {
                        delay(10 * 60 * 1000L) // Fallback sleep
                    }
                } catch (e: Exception) {
                    delay(5 * 60 * 1000L)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendBreathingNotification(prayerName: String, targetTimeMs: Long, diffMs: Long) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Native countdown timer
        val isCountDown = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        val builder = NotificationCompat.Builder(this, CHANNEL_BREATHING)
            .setContentTitle("Next: $prayerName")
            .setContentText("Time remaining for prayer")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOnlyAlertOnce(false)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isCountDown) {
            builder.setUsesChronometer(true)
            builder.setChronometerCountDown(true)
            builder.setWhen(targetTimeMs)
        } else {
            // Fallback for older Android
            val h = diffMs / 3600000
            val m = (diffMs / 60000) % 60
            builder.setContentText("In ${h}h ${m}m")
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(99, builder.build()) // ID 99 so it replaces the old one, doesn't stack
    }

    private fun buildSilentNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_SILENT)
            .setContentTitle("Jannah Brain is active")
            .setContentText("Monitoring prayer times and app locks.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Completely silent, grouped at bottom
            .build()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            
            // Silent channel for the ongoing service
            val silentChannel = NotificationChannel(CHANNEL_SILENT, "System Guardian", NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
            }
            nm.createNotificationChannel(silentChannel)

            // High priority channel for the "Call-like" prayer popups
            val breathingChannel = NotificationChannel(CHANNEL_BREATHING, "Prayer Countdowns", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts you as prayer time approaches"
                enableVibration(true)
            }
            nm.createNotificationChannel(breathingChannel)
        }
    }
}
