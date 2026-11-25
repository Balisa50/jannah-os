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
        if (lat == 0.0 && lon == 0.0) return

        val offsetMinutes = minutesBeforeFajr.coerceIn(0, 240)
        val now = System.currentTimeMillis()
        var targetDate = Calendar.getInstance()
        var fajrTime = SalahScheduler.timesForDate(ctx, lat, lon, targetDate)["fajr"] ?: return
        var triggerTime = fajrTime - offsetMinutes * 60_000L

        // The alarm can fire before Fajr. In that case today's Fajr is still
        // in the future, but today's Tahajjud trigger has already passed.
        if (triggerTime <= now) {
            targetDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            fajrTime = SalahScheduler.timesForDate(ctx, lat, lon, targetDate)["fajr"] ?: return
            triggerTime = fajrTime - offsetMinutes * 60_000L
        }

        if (triggerTime <= now) return

        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TahajjudAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

    fun cancel(ctx: Context) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TahajjudAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
