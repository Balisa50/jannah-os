package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Real prayer times from Aladhan. Free, no key, global coverage.
// User picks their city or coordinates. Times update daily automatically.
object PrayerApi {

    data class Times(
        val fajr: String, val sunrise: String, val dhuhr: String,
        val asr: String, val maghrib: String, val isha: String,
        val date: String
    )

    // Fetch today's times for a city. Cached for 24 hours per city.
    fun fetchByCity(
        context: Context,
        city: String,
        country: String,
        method: Int = 3 // Muslim World League
    ): Times? {
        val cacheKey = "city_${city.lowercase()}_${country.lowercase()}_$method"
        val cached = readCache(context, cacheKey)
        if (cached != null) return cached

        return try {
            val url = "https://api.aladhan.com/v1/timingsByCity?city=" +
                URLEncoder.encode(city, "UTF-8") +
                "&country=" + URLEncoder.encode(country, "UTF-8") +
                "&method=$method"
            val body = httpGet(url) ?: return null
            val times = parseTimes(body) ?: return null
            writeCache(context, cacheKey, times)
            times
        } catch (e: Exception) { null }
    }

    // Fetch by coordinates.
    fun fetchByCoords(
        context: Context,
        lat: Double,
        lon: Double,
        method: Int = 3
    ): Times? {
        val cacheKey = "coords_${lat}_${lon}_$method"
        val cached = readCache(context, cacheKey)
        if (cached != null) return cached

        return try {
            val url = "https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lon&method=$method"
            val body = httpGet(url) ?: return null
            val times = parseTimes(body) ?: return null
            writeCache(context, cacheKey, times)
            times
        } catch (e: Exception) { null }
    }

    private fun parseTimes(body: String): Times? {
        return try {
            val root = JSONObject(body)
            val data = root.getJSONObject("data")
            val timings = data.getJSONObject("timings")
            val date = data.getJSONObject("date").getString("readable")
            Times(
                fajr = clean(timings.getString("Fajr")),
                sunrise = clean(timings.getString("Sunrise")),
                dhuhr = clean(timings.getString("Dhuhr")),
                asr = clean(timings.getString("Asr")),
                maghrib = clean(timings.getString("Maghrib")),
                isha = clean(timings.getString("Isha")),
                date = date
            )
        } catch (e: Exception) { null }
    }

    private fun clean(t: String): String = t.substringBefore(" ").trim()

    private fun httpGet(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) { conn.disconnect(); return null }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            body
        } catch (e: Exception) { null }
    }

    // Cache per day. Key includes the date so it auto-invalidates.
    private fun readCache(context: Context, key: String): Times? {
        val p = context.getSharedPreferences("jannah_prayer_cache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val stored = p.getString("${key}_date", "")
        if (stored != today) return null
        val json = p.getString(key, null) ?: return null
        return try {
            val o = JSONObject(json)
            Times(
                o.getString("fajr"), o.getString("sunrise"), o.getString("dhuhr"),
                o.getString("asr"), o.getString("maghrib"), o.getString("isha"),
                o.optString("date", "")
            )
        } catch (e: Exception) { null }
    }

    private fun writeCache(context: Context, key: String, t: Times) {
        val p = context.getSharedPreferences("jannah_prayer_cache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val json = JSONObject().apply {
            put("fajr", t.fajr); put("sunrise", t.sunrise); put("dhuhr", t.dhuhr)
            put("asr", t.asr); put("maghrib", t.maghrib); put("isha", t.isha)
            put("date", t.date)
        }.toString()
        p.edit().putString(key, json).putString("${key}_date", today).apply()
    }

    // Convert HH:mm string to today's epoch millis.
    fun timeToMillis(hhmm: String): Long {
        return try {
            val parts = hhmm.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val c = java.util.Calendar.getInstance()
            c.set(java.util.Calendar.HOUR_OF_DAY, h)
            c.set(java.util.Calendar.MINUTE, m)
            c.set(java.util.Calendar.SECOND, 0)
            c.set(java.util.Calendar.MILLISECOND, 0)
            c.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
