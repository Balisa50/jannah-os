package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule the adaptive prayer alarms
            try {
                PrayerReminderScheduler.scheduleNextReminder(context)
            } catch (_: Exception) {}
        }
    }
}
