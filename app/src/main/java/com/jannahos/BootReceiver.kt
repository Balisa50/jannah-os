package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> restoreSchedules(context.applicationContext)
            else -> return
        }
    }

    private fun restoreSchedules(context: Context) {
        // Each schedule is isolated: one unavailable permission or malformed
        // preference must not prevent the other reminders from being restored.
        runCatching { PrayerReminderScheduler.scheduleAllForToday(context) }

        if (Settings.isBedtimeLockEnabled(context)) {
            runCatching { BedtimeScheduler.schedule(context) }
        }

        if (Settings.isTahajjudEnabled(context)) {
            runCatching {
                TahajjudScheduler.scheduleAlarm(
                    context,
                    Settings.getTahajjudOffset(context)
                )
            }
        }

        runCatching { FridayKahfScheduler.scheduleAll(context) }
    }
}
