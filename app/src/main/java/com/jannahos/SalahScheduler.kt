package com.jannahos

import android.content.Context
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

object SalahScheduler {
    
    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        return try {
            val coordinates = Coordinates(lat, lon)
            
            // DYNAMIC TIMEZONE: Calculate offset based on longitude (15 degrees = 1 hour)
            val timezoneOffset = (lon / 15.0).roundToInt()
            val timezoneString = if (timezoneOffset >= 0) "GMT+$timezoneOffset" else "GMT$timezoneOffset"
            val tz = TimeZone.getTimeZone(timezoneString)
            
            val calendar = Calendar.getInstance(tz)
            val dateComponents = DateComponents.from(calendar.time)
            val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

            mapOf(
                "fajr" to prayerTimes.fajr.time,
                "dhuhr" to prayerTimes.dhuhr.time,
                "asr" to prayerTimes.asr.time,
                "maghrib" to prayerTimes.maghrib.time,
                "isha" to prayerTimes.isha.time
            )
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            mapOf(
                "fajr" to (now + 3600000), "dhuhr" to (now + 7200000),
                "asr" to (now + 10800000), "maghrib" to (now + 14400000), "isha" to (now + 18000000)
            )
        }
    }

    fun todayTimes(ctx: Context, lat: Double, lon: Double): Map<String, Long> {
        return todayTimes(lat, lon)
    }

    fun localTimes(lat: Double, lon: Double): Map<String, Long> = todayTimes(lat, lon)
}
