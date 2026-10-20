package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerTimesApi {
    private const val BASE_URL = "https://api.aladhan.com/v1/timingsByCity"

    data class PrayerTimes(val fajr: Long, val dhuhr: Long, val asr: Long, val maghrib: Long, val isha: Long)

    fun fetch(city: String, country: String): PrayerTimes? {
        return try {
            val method = CountryMethodMapper.getMethodForCountry(country)
            val url = URL("$BASE_URL?city=${city.replace(" ", "%20")}&country=${country.replace(" ", "%20")}&method=$method")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val timings = root.getJSONObject("data").getJSONObject("timings")
                
                val fmt = SimpleDateFormat("HH:mm", Locale.US)
                val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val today = dateFmt.format(Date())
                
                PrayerTimes(
                    fajr = parseTime(timings.getString("Fajr"), today, fmt),
                    dhuhr = parseTime(timings.getString("Dhuhr"), today, fmt),
                    asr = parseTime(timings.getString("Asr"), today, fmt),
                    maghrib = parseTime(timings.getString("Maghrib"), today, fmt),
                    isha = parseTime(timings.getString("Isha"), today, fmt)
                )
            } else {
                conn.disconnect()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTime(time: String, date: String, fmt: SimpleDateFormat): Long {
        return try {
            val dt = "$date $time"
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            parser.parse(dt)?.time ?: 0L
        } catch (e: Exception) { 0L }
    }
}
