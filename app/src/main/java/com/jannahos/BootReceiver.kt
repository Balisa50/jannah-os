package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Settings.isTahajjudEnabled(context)) {
            TahajjudScheduler.scheduleAlarm(context, Settings.getTahajjudOffset(context))
        }
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule the adaptive prayer alarms
            try {
                PrayerReminderScheduler.scheduleNext(context)
            FridayKahfScheduler.scheduleAll(context)
            } catch (_: Exception) {}
        }
    }
}
