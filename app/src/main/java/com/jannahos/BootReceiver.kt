package com.jannahos

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> restoreSchedules(context.applicationContext)
            else -> return
        }
    }

    private fun restoreSchedules(context: Context) {
        // Isolate schedules so a missing optional permission cannot block
        // restoration of unrelated reminders.
        runCatching { PrayerReminderScheduler.scheduleAllForToday(context) }

        if (Settings.isBedtimeLockEnabled(context)) {
            runCatching { BedtimeScheduler.schedule(context) }
        }

        if (Settings.isTahajjudEnabled(context)) {
            runCatching {
                TahajjudScheduler.scheduleAlarm(context, Settings.getTahajjudOffset(context))
            }
        }

        if (QuranReadingLock.enabled(context) && !QuranReadingLock.isDoneToday(context)) {
            runCatching {
                if (QuranReadingLock.isActiveToday(context)) {
                    QuranLockScheduler.scheduleNextDay(context)
                } else {
                    QuranLockScheduler.scheduleRandomLock(context)
                }
            }
        }

        runCatching { FridayKahfScheduler.scheduleAll(context) }
    }
}
