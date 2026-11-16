package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PrayerReminderScheduler {
    fun scheduleAllForToday(ctx: Context) {
        val lat = Settings.lat(ctx)
        val lon = Settings.lon(ctx)
        if (lat == 0.0 || lon == 0.0) return

        val times = SalahScheduler.todayTimes(ctx, lat, lon)
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        val now = System.currentTimeMillis()

        for (key in ordered) {
            val adhanTime = times[key] ?: continue
            if (adhanTime > now) {
                schedulePrayerSequence(ctx, SalahTracker.label(key), adhanTime, key == "dhuhr" && isFriday())
            }
        }
    }

    private fun schedulePrayerSequence(ctx: Context, prayerName: String, adhanTime: Long, isJummah: Boolean) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val iqamahGapMinutes = if (isJummah) 45 else Settings.getIqamahGap(ctx)
        val iqamahTime = adhanTime + (iqamahGapMinutes * 60 * 1000L)

        // 1. The Adhan Alarm
        setAlarm(ctx, am, prayerName, adhanTime, "adhan", 1)

        // 2. Reminder Vibrations (e.g., at 3, 6 minutes after Adhan)
        val reminderIntervals = listOf(3, 6, iqamahGapMinutes - 2) // 2 mins before iqamah
        reminderIntervals.forEachIndexed { index, mins ->
            val triggerTime = adhanTime + (mins * 60 * 1000L)
            if (triggerTime < iqamahTime) {
                setAlarm(ctx, am, prayerName, triggerTime, "reminder", index + 2)
            }
        }

        // 3. The Iqamah Lock
        setAlarm(ctx, am, prayerName, iqamahTime, "iqamah", 99)
    }

    private fun setAlarm(ctx: Context, am: AlarmManager, prayer: String, time: Long, type: String, code: Int) {
        val intent = Intent(ctx, PrayerReminderReceiver::class.java).apply {
            putExtra("prayer", prayer)
            putExtra("type", type)
        }
        val pi = PendingIntent.getBroadcast(ctx, prayer.hashCode() + code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        }
    }

    private fun isFriday(): Boolean = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
}
