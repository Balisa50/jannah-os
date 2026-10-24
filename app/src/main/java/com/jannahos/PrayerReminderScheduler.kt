package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PrayerReminderScheduler {
    
    // The exact escalating intervals (in minutes before prayer)
    private val INTERVALS = intArrayOf(60, 30, 15, 10, 5, 3, 2, 1)

    fun scheduleNext(ctx: Context) {
        val times = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
        val now = System.currentTimeMillis()
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        
        // Find the next upcoming prayer
        val nextKey = ordered.firstOrNull { (times[it] ?: 0L) > now }
        val nextTime = nextKey?.let { times[it] } ?: ((times["fajr"] ?: now) + 24L * 60 * 60 * 1000)

        var triggerTime = -1L

        // Find the next interval that hasn't happened yet
        for (interval in INTERVALS) {
            val reminderTime = nextTime - (interval * 60_000L)
            if (reminderTime > now) {
                triggerTime = reminderTime
                break
            }
        }

        // If all intervals for today's next prayer have passed, trigger at the prayer time itself
        if (triggerTime == -1L) {
            triggerTime = nextTime
        }

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
