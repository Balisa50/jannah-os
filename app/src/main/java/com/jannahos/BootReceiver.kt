package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Reschedule all alarms on boot
        try {
            val lat = Settings.lat(context)
            val lon = Settings.lon(context)
            val times = SalahScheduler.todayTimes(lat, lon)
            val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
            for (key in ordered) {
                val t = times[key] ?: continue
                if (t > System.currentTimeMillis()) {
                    PrayerReminderScheduler.scheduleReminders(context, SalahTracker.label(key), t)
                }
            }
        } catch (_: Throwable) {}

        try { RamadanAlarmScheduler.scheduleAlarms(context) } catch (_: Throwable) {}

        try {
            if (Settings.isTahajjudEnabled(context)) {
                TahajjudScheduler.scheduleAlarm(context, Settings.getTahajjudOffset(context))
            }
        } catch (_: Throwable) {}

        try {
            if (QuranReadingLock.enabled(context)) {
                QuranLockScheduler.scheduleRandomLock(context)
            }
        } catch (_: Throwable) {}
    }
}
