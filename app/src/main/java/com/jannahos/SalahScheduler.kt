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

    fun canUseExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun schedule(context: Context, lat: Double, lon: Double) {
        val map = todayTimes(lat, lon)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        for ((name, code) in PRAYERS) {
            val t = map[name] ?: continue
            if (t <= now) continue
            val pi = pendingIntent(context, name, code)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
            }
        }
    }

    private fun pendingIntent(context: Context, name: String, code: Int): PendingIntent {
        val intent = Intent(context, SalahReceiver::class.java).apply {
            action = "com.jannahos.SALAH"
            putExtra("prayer", name)
        }
        return PendingIntent.getBroadcast(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // API (with fallback)
    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        val ctx: android.content.Context? = App.instance
        if (ctx != null) {
            val city = CityStore.name(ctx)
            val country = CityStore.country(ctx)
            if (city.isNotBlank() && country.isNotBlank()) {
                val apiTimes = PrayerTimesApi.fetch(city, country)
                if (apiTimes != null) {
                    return mapOf(
                        "fajr" to apiTimes.fajr,
                        "dhuhr" to apiTimes.dhuhr,
                        "asr" to apiTimes.asr,
                        "maghrib" to apiTimes.maghrib,
                        "isha" to apiTimes.isha
                    )
                }
            }
        }
        return calculateTimesLocally(lat, lon)
    }

    // Local offline calculation
    fun localTimes(lat: Double, lon: Double): Map<String, Long> {
        return calculateTimesLocally(lat, lon)
    }

    fun calculateTimesLocally(lat: Double, lon: Double): Map<String, Long> {
        // Fallback logic missing
        return emptyMap()
    }
}
