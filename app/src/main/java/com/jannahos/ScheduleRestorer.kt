package com.jannahos

import android.content.Context

/**
 * Rebuilds user-enabled schedules after boot, app update, date/time changes,
 * and an ordinary app launch (which also recovers schedules after force-stop).
 *
 * Each feature is isolated so one unavailable capability cannot suppress the
 * remaining schedules.
 */
object ScheduleRestorer {
    fun restore(context: Context) {
        val appContext = context.applicationContext

        runCatching { PrayerReminderScheduler.scheduleAllForToday(appContext) }
        runCatching { com.jannahos.siraj.actions.SirajReminderScheduler.restore(appContext) }

        if (Settings.isBedtimeLockEnabled(appContext)) {
            runCatching { BedtimeScheduler.schedule(appContext) }
        }

        if (Settings.isTahajjudEnabled(appContext)) {
            runCatching {
                TahajjudScheduler.scheduleAlarm(
                    appContext,
                    Settings.getTahajjudOffset(appContext)
                )
            }
        }

        if (QuranReadingLock.enabled(appContext)) {
            runCatching {
                if (QuranReadingLock.isDoneToday(appContext) ||
                    QuranReadingLock.isActiveToday(appContext)
                ) {
                    QuranLockScheduler.scheduleNextDay(appContext)
                } else {
                    QuranLockScheduler.scheduleRandomLock(appContext)
                }
            }
        }

        runCatching { FridayKahfScheduler.scheduleAll(appContext) }
    }
}
