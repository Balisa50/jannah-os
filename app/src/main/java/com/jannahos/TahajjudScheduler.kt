package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import java.util.Date

object TahajjudScheduler {

    private const val REQ_CODE = 4201
    private const val PREFS = "jannah_prefs"
    private const val KEY_NEXT = "next_tahajjud_ms"

    fun schedule(context: Context, at: Date) {
        if (!TahajjudPlan.isOnTonight(context)) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context)

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            am.canScheduleExactAlarms() else true

        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.time, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.time, pi)
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_NEXT, at.time).apply()
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_NEXT).apply()
    }

    fun nextScheduled(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_NEXT, 0L)

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TahajjudAlarmReceiver::class.java)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQ_CODE, intent, flags)
    }

    // Compute tonight's tahajjud time for a saved location.
    fun computeTonight(lat: Double, lon: Double): Date {
        val now = Date()
        val lastThird = PrayerCalculator.lastThirdOfNight(lat, lon, now)

        // If last third already passed, target tomorrow's.
        val cal = Calendar.getInstance()
        cal.time = lastThird
        if (cal.time.before(now)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            return PrayerCalculator.lastThirdOfNight(lat, lon, cal.time)
        }
        return lastThird
    }
}
