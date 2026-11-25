package com.jannahos

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

object PrayerCalculator {

    // The same persisted method is used by the scheduler and all prayer-derived
    // calculations, avoiding contradictory Fajr/Maghrib values in the app.
    fun forDate(lat: Double, lon: Double, at: Date): PrayerTimes {
        require(lat.isFinite() && lat in -90.0..90.0) { "Latitude is outside valid bounds" }
        require(lon.isFinite() && lon in -180.0..180.0) { "Longitude is outside valid bounds" }

        val coords = Coordinates(lat, lon)
        val method = App.instance?.let { Settings.method(it) }
        val params = PrayerCalculationMethods.fromPreference(method).parameters
        val date = DateComponents.from(at)
        return PrayerTimes(coords, date, params)
    }

    // Night begins at Maghrib and ends at the following Fajr.
    fun lastThirdOfNight(lat: Double, lon: Double, at: Date): Date {
        val times = forDate(lat, lon, at)
        val maghrib = times.maghrib.time
        val fajr = times.fajr.time
        val endOfNight = if (fajr <= maghrib) fajr + 24L * 60 * 60 * 1000 else fajr
        val span = endOfNight - maghrib
        return Date(maghrib + (span * 2 / 3))
    }
}
