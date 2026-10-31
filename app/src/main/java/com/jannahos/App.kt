package com.jannahos

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule alarms on app startup
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
        } catch (e: Exception) { e.printStackTrace() }

        try { RamadanAlarmScheduler.scheduleAlarms(this) } catch (e: Exception) {}
        try {
            if (Settings.isTahajjudEnabled(this)) {
                TahajjudScheduler.scheduleAlarm(this, Settings.getTahajjudOffset(this))
            }
        } catch (e: Exception) {}
    }

    companion object {
        fun latestCrash(context: Context): String? {
            val f = java.io.File(context.filesDir, "last_crash.txt")
            return if (f.exists()) f.readText() else null
        }
    }
}
