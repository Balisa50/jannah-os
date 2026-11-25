package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object PrayerReminderScheduler {
    private val orderedPrayers = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    fun scheduleAllForToday(ctx: Context) {
        scheduleDate(ctx, Calendar.getInstance())
        scheduleNextDay(ctx)
    }

    // Called by alarm receivers as a rolling horizon, so schedules remain
    // populated even when the user does not reopen the app every day.
    fun scheduleNextDay(ctx: Context) {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleDate(ctx, tomorrow)
    }

    private fun scheduleDate(ctx: Context, date: Calendar) {
        val lat = Settings.lat(ctx)
        val lon = Settings.lon(ctx)
        if (lat == 0.0 && lon == 0.0) return

        val times = SalahScheduler.timesForDate(ctx, lat, lon, date)
        val now = System.currentTimeMillis()
        val dateKey = date.get(Calendar.YEAR) * 1000 + date.get(Calendar.DAY_OF_YEAR)
        val isFriday = date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

        for ((index, key) in orderedPrayers.withIndex()) {
            val adhanTime = times[key] ?: continue
            if (adhanTime > now) {
                schedulePrayerSequence(
                    ctx = ctx,
                    prayerName = SalahTracker.label(key),
                    adhanTime = adhanTime,
                    isJummah = key == "dhuhr" && isFriday,
                    dateKey = dateKey,
                    prayerIndex = index
                )
            }
        }
    }

    private fun schedulePrayerSequence(
        ctx: Context,
        prayerName: String,
        adhanTime: Long,
        isJummah: Boolean,
        dateKey: Int,
        prayerIndex: Int
    ) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val iqamahGapMinutes = if (isJummah) 45 else Settings.getIqamahGap(ctx).coerceIn(5, 180)
        val iqamahTime = adhanTime + iqamahGapMinutes * 60_000L

        setAlarm(ctx, alarmManager, prayerName, adhanTime, "adhan", dateKey, prayerIndex, 1)

        listOf(3, 6, iqamahGapMinutes - 2).forEachIndexed { index, minutes ->
            val triggerTime = adhanTime + minutes * 60_000L
            if (minutes > 0 && triggerTime < iqamahTime) {
                setAlarm(
                    ctx, alarmManager, prayerName, triggerTime, "reminder",
                    dateKey, prayerIndex, index + 2
                )
            }
        }

        setAlarm(ctx, alarmManager, prayerName, iqamahTime, "iqamah", dateKey, prayerIndex, 99)
    }

    private fun setAlarm(
        ctx: Context,
        alarmManager: AlarmManager,
        prayer: String,
        time: Long,
        type: String,
        dateKey: Int,
        prayerIndex: Int,
        code: Int
    ) {
        val intent = Intent(ctx, PrayerReminderReceiver::class.java).apply {
            putExtra("prayer", prayer)
            putExtra("type", type)
        }
        val requestCode = dateKey + (prayerIndex * 100) + code
        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }
}
