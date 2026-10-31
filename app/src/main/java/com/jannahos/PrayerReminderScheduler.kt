package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PrayerReminderScheduler {
    // Industrial standard intervals (in minutes before prayer)
    private val intervals = listOf(60, 30, 15, 10, 5)

    fun scheduleReminders(ctx: Context, prayerName: String, prayerTimeMs: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        intervals.forEachIndexed { index, minutes ->
            val triggerTime = prayerTimeMs - (minutes * 60 * 1000L)
            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(ctx, PrayerReminderReceiver::class.java).apply {
                    putExtra("prayer", prayerName)
                    putExtra("minutes", minutes)
                }
                // Use a unique request code for each interval
                val pi = PendingIntent.getBroadcast(ctx, prayerName.hashCode() + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am.canScheduleExactAlarms()) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
                    } else {
                        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
                    }
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
                }
            }
        }
    }
}
