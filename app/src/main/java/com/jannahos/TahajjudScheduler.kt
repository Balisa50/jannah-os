package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object TahajjudScheduler {
    private const val REQ_CODE = 999

    fun scheduleAlarm(ctx: Context, minutesBeforeFajr: Int) {
        val lat = Settings.lat(ctx)
        val lon = Settings.lon(ctx)
        val times = SalahScheduler.todayTimes(ctx, lat, lon)
        var fajrTime = times["fajr"] ?: return

        // If Fajr has already passed today, schedule for tomorrow's Fajr
        if (fajrTime < System.currentTimeMillis()) {
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowTimes = SalahScheduler.todayTimes(ctx, lat, lon) // Assuming this gives today's times
            fajrTime = tomorrowTimes["fajr"]?.plus(24 * 60 * 60 * 1000L) ?: return
        }

        val triggerTime = fajrTime - (minutesBeforeFajr * 60 * 1000L)
        
        // Don't schedule if the time is in the past
        if (triggerTime < System.currentTimeMillis()) return

        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TahajjudAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(ctx, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TahajjudAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(ctx, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
    }
}
