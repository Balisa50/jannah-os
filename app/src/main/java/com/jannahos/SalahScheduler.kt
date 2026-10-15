package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

object SalahScheduler {

    private val PRAYERS = listOf(
        "fajr" to 5101,
        "dhuhr" to 5102,
        "asr" to 5103,
        "maghrib" to 5104,
        "isha" to 5105
    )

    // Returns true if exact alarms are allowed. If false, the caller should send the user
    // to Settings → Special access → Alarms & reminders.
    fun canUseExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun schedule(context: Context, lat: Double, lon: Double) {
        val map = todayTimesMs(lat, lon)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        for ((name, code) in PRAYERS) {
            val t = map[name] ?: continue
            if (t <= now) continue
            val pi = pendingIntent(context, name, code)
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                am.canScheduleExactAlarms() else true
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
            }
        }

        val midnight = nextMidnightMs()
        val pi = pendingIntent(context, "reschedule", 5199)
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            am.canScheduleExactAlarms() else true
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight, pi)
        }
    }

    fun todayTimes(lat: Double, lon: Double): Map<String, Date> {
        val t = prayerTimes(lat, lon, Date())
        return mapOf(
            "fajr" to t.fajr,
            "dhuhr" to t.dhuhr,
            "asr" to t.asr,
            "maghrib" to t.maghrib,
            "isha" to t.isha
        )
    }

    private fun todayTimesMs(lat: Double, lon: Double): Map<String, Long> =
        todayTimes(lat, lon).mapValues { it.value.time }

    private fun prayerTimes(lat: Double, lon: Double, at: Date): PrayerTimes {
        val coords = Coordinates(lat, lon)
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        val date = DateComponents.from(at)
        return PrayerTimes(coords, date, params)
    }

    private fun nextMidnightMs(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 5)
        cal.set(java.util.Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    private fun pendingIntent(context: Context, name: String, code: Int): PendingIntent {
        val intent = Intent(context, SalahReceiver::class.java).apply {
            putExtra("prayer", name)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, code, intent, flags)
    }
}
