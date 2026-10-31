package com.jannahos

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule all prayer reminders, Ramadan alarms, and Tahajjud on startup
        try {
            val lat = Settings.lat(this)
            val lon = Settings.lon(this)
            val times = SalahScheduler.todayTimes(lat, lon)
            val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
            for (key in ordered) {
                val t = times[key] ?: continue
                if (t > System.currentTimeMillis()) {
                    PrayerReminderScheduler.scheduleReminders(this, SalahTracker.label(key), t)
                }
            }
        } catch (_: Throwable) {}

        try { RamadanAlarmScheduler.scheduleAlarms(this) } catch (_: Throwable) {}

        try {
            if (Settings.isTahajjudEnabled(this)) {
                TahajjudScheduler.scheduleAlarm(this, Settings.getTahajjudOffset(this))
            }
        } catch (_: Throwable) {}
    }

    companion object {
        fun latestCrash(context: android.content.Context): String? {
            val f = java.io.File(context.filesDir, "last_crash.txt")
            return if (f.exists()) f.readText() else null
        }
    }
}
