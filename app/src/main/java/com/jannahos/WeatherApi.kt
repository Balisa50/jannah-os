package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Free, no-key weather via open-meteo. Used to pick context-appropriate adhkar.
object WeatherApi {

    data class Current(
        val temperatureC: Double,
        val precipitationMm: Double,
        val weatherCode: Int
    )

    fun fetch(lat: Double, lon: Double): Current? {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat" +
            "&longitude=$lon&current=temperature_2m,precipitation,weather_code"
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val cur = root.getJSONObject("current")
            Current(
                cur.optDouble("temperature_2m", 0.0),
                cur.optDouble("precipitation", 0.0),
                cur.optInt("weather_code", 0)
            )
        } catch (e: Exception) {
            null
        }
    }

    // WMO weather codes: 51-57 drizzle, 61-67 rain, 80-82 showers, 95+ thunder.
    fun isRaining(code: Int): Boolean =
        code in 51..67 || code in 80..82 || code >= 95

    fun isHot(tempC: Double): Boolean = tempC >= 32.0
}
