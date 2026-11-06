package com.jannahos

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date

object SalahScheduler {
    
    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        return try {
            val coordinates = Coordinates(lat, lon)
            
            // Dynamically use the device's current timezone
            val calendar = Calendar.getInstance()
            val dateComponents = DateComponents.from(calendar.time)
            
            // Use Muslim World League calculation method
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
                "fajr" to (now - 3600000),
                "dhuhr" to (now + 3600000),
                "asr" to (now + 7200000),
                "maghrib" to (now + 10800000),
                "isha" to (now + 14400000)
            )
        }
    }

    fun localTimes(lat: Double, lon: Double): Map<String, Long> {
        return todayTimes(lat, lon)
    }
}
