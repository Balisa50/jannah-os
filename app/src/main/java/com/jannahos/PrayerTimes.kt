package com.jannahos

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

object PrayerCalculator {

    // Default: Muslim World League. User-configurable later.
    fun forDate(lat: Double, lon: Double, at: Date): PrayerTimes {
        val coords = Coordinates(lat, lon)
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        val date = DateComponents.from(at)
        return PrayerTimes(coords, date, params)
    }

    // "Last third of the night" between Maghrib and Fajr.
    // Standard fiqh position: night begins at Maghrib, ends at Fajr.
    fun lastThirdOfNight(lat: Double, lon: Double, at: Date): Date {
        val times = forDate(lat, lon, at)
        val maghrib = times.maghrib.time
        val fajr = times.fajr.time
        val span = fajr - maghrib
        return Date(maghrib + (span * 2 / 3))
    }
}
