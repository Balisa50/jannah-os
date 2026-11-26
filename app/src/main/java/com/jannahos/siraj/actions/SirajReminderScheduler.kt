package com.jannahos.siraj.actions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

/**
 * Persists user-created reminders and restores their alarms after reboot,
 * app updates, date changes and exact-alarm permission changes.
 */
object SirajReminderScheduler {
    private const val PREFS = "siraj_user_reminders"
    private const val KEY_IDS = "ids"
    private const val TITLE_PREFIX = "title_"
    private const val TIME_PREFIX = "time_"

    private data class StoredReminder(
        val id: String,
        val title: String,
        val triggerAtMillis: Long
    )

    fun schedule(context: Context, title: String, triggerAtMillis: Long) {
        require(title.isNotBlank()) { "A reminder needs a title." }
        require(triggerAtMillis > System.currentTimeMillis()) { "The reminder time must be in the future." }

        val safeTitle = title.trim().take(100)
        val id = "${triggerAtMillis}-${safeTitle.hashCode()}"
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().toMutableSet()
        ids.add(id)
        prefs.edit()
            .putStringSet(KEY_IDS, ids)
            .putString(TITLE_PREFIX + id, safeTitle)
            .putLong(TIME_PREFIX + id, triggerAtMillis)
            .apply()

        arm(context.applicationContext, StoredReminder(id, safeTitle, triggerAtMillis))
    }

    fun restore(context: Context) {
        val now = System.currentTimeMillis()
        reminders(context).forEach { reminder ->
            if (reminder.triggerAtMillis <= now) {
                remove(context, reminder.id)
            } else {
                runCatching { arm(context.applicationContext, reminder) }
            }
        }
    }

    fun remove(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().toMutableSet()
        ids.remove(id)
        prefs.edit()
            .putStringSet(KEY_IDS, ids)
            .remove(TITLE_PREFIX + id)
            .remove(TIME_PREFIX + id)
            .apply()
    }

    private fun reminders(context: Context): List<StoredReminder> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().mapNotNull { id ->
            val title = prefs.getString(TITLE_PREFIX + id, null) ?: return@mapNotNull null
            val trigger = prefs.getLong(TIME_PREFIX + id, 0L)
            if (trigger <= 0L) null else StoredReminder(id, title, trigger)
        }
    }

    private fun arm(context: Context, reminder: StoredReminder) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: error("Alarm service is unavailable.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val intent = Intent(context, SirajReminderReceiver::class.java).apply {
            data = Uri.parse("siraj-reminder://local/${reminder.id}")
            putExtra(SirajReminderReceiver.EXTRA_ID, reminder.id)
            putExtra(SirajReminderReceiver.EXTRA_TITLE, reminder.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAtMillis,
            pendingIntent
        )
    }
}
