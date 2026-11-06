package com.jannahos

import android.content.Context
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar

object SalahScheduler {
    fun todayTimes(ctx: Context, lat: Double, lon: Double): Map<String, Long> {
        return try {
            val coordinates = Coordinates(lat, lon)
            val calendar = Calendar.getInstance()
            val isFriday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val dateComponents = DateComponents.from(calendar.time)
            val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
            
            // Get the user's configured gap
            val gap = if (isFriday) 45 else Settings.getIqamahGap(ctx)
            val gapMs = gap * 60 * 1000L

            // We store the IQAMAH time as the trigger time for locks and alarms
            mapOf(
                "fajr" to (prayerTimes.fajr.time + gapMs),
                "dhuhr" to (prayerTimes.dhuhr.time + gapMs),
                "asr" to (prayerTimes.asr.time + gapMs),
                "maghrib" to (prayerTimes.maghrib.time + gapMs), // Maghrib is usually immediate
                "isha" to (prayerTimes.isha.time + gapMs)
            )
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            mapOf(
                "fajr" to (now + 3600000), "dhuhr" to (now + 7200000),
                "asr" to (now + 10800000), "maghrib" to (now + 14400000), "isha" to (now + 18000000)
            )
        }
    }
    
    fun isFriday(): Boolean = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
}
