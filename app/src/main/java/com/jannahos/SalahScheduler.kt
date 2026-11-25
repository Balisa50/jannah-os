package com.jannahos

import android.content.Context
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar

object SalahScheduler {
    fun todayTimes(ctx: Context, lat: Double, lon: Double): Map<String, Long> =
        timesForDate(ctx, lat, lon, Calendar.getInstance())

    fun timesForDate(
        ctx: Context,
        lat: Double,
        lon: Double,
        date: Calendar
    ): Map<String, Long> {
        if (lat == 0.0 && lon == 0.0) return emptyMap()

        return try {
            val coordinates = Coordinates(lat, lon)
            // Use the device's actual civil timezone. Longitude/15 is only a
            // rough solar offset and fails around DST and timezone boundaries.
            val dateComponents = DateComponents.from(date.time)
            val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

            mapOf(
                "fajr" to prayerTimes.fajr.time,
                "dhuhr" to prayerTimes.dhuhr.time,
                "asr" to prayerTimes.asr.time,
                "maghrib" to prayerTimes.maghrib.time,
                "isha" to prayerTimes.isha.time
            )
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        val ctx = App.instance ?: return emptyMap()
        return todayTimes(ctx, lat, lon)
    }
}
