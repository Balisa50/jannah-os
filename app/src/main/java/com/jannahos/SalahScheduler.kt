package com.jannahos

import android.content.Context
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

object SalahScheduler {
    
    // Safely calculates times locally. No internet required. Never crashes.
    fun todayTimes(lat: Double, lon: Double): Map<String, Long> {
        return try {
            // Fallback to Accra, Ghana coordinates if lat/lon are 0.0 (which happens if user skipped)
            val safeLat = if (lat == 0.0) 5.6037 else lat
            val safeLon = if (lon == 0.0) -0.1870 else lon
            
            val coordinates = Coordinates(safeLat, safeLon)
            val dateComponents = DateComponents.from(Date())
            
            // Use Muslim World League calculation method (Standard for West Africa)
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
            // If math fails (it won't), return fake times so UI doesn't crash
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
