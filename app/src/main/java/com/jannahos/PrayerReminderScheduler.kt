package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PrayerReminderScheduler {

    fun scheduleNextReminder(ctx: Context) {
        val times = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
        val now = System.currentTimeMillis()
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        
        val nextKey = ordered.firstOrNull { (times[it] ?: 0L) > now }
        val nextTime = nextKey?.let { times[it] } ?: (times["fajr"] ?: now) + 24L * 60 * 60 * 1000

        val diff = nextTime - now
        
        // Adaptive Intervals
        val intervalMs = when {
            diff > 2 * 60 * 60 * 1000L -> 60 * 60 * 1000L // > 2 hours: every hour
            diff > 60 * 60 * 1000L -> 30 * 60 * 1000L     // 1-2 hours: every 30 mins
            diff > 30 * 60 * 1000L -> 15 * 60 * 1000L     // 30-60 mins: every 15 mins
            diff > 10 * 60 * 1000L -> 5 * 60 * 1000L      // 10-30 mins: every 5 mins
            else -> 2 * 60 * 1000L                        // < 10 mins: every 2 mins
        }

        val triggerTime = minOf(now + intervalMs, nextTime)

        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, PrayerReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
