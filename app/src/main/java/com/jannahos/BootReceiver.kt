package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
        } catch (e: Exception) { e.printStackTrace() }

        try { RamadanAlarmScheduler.scheduleAlarms(context) } catch (e: Exception) {}
        try {
            if (Settings.isTahajjudEnabled(context)) {
                TahajjudScheduler.scheduleAlarm(context, Settings.getTahajjudOffset(context))
            }
        } catch (e: Exception) {}
        try {
            if (QuranReadingLock.enabled(context)) {
                QuranLockScheduler.scheduleRandomLock(context)
            }
        } catch (e: Exception) {}
    }
}
