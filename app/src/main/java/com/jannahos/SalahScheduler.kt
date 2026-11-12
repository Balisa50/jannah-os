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
    fun todayTimes(ctx: Context, lat: Double, lon: Double): Map<String, Long> {
        // If location is not set, return empty. The UI will ask the user to set it.
        if (lat == 0.0 && lon == 0.0) return emptyMap()

        return try {
            val coordinates = Coordinates(lat, lon)
            val calendar = Calendar.getInstance()
            
            // Dynamic Timezone Calculation
            val timezoneOffset = (lon / 15.0).roundToInt()
            val tz = TimeZone.getTimeZone(if (timezoneOffset >= 0) "GMT+$timezoneOffset" else "GMT$timezoneOffset")
            calendar.timeZone = tz
            
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
            emptyMap()
        }
    }
    
    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        val ctx = App.instance ?: return emptyMap()
        return todayTimes(ctx, lat, lon)
    }
    
    fun localTimes(lat: Double, lon: Double): Map<String, Long> = todayTimes(lat, lon)
}
