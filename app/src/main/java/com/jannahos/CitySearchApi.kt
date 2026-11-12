package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object CitySearchApi {

    // Result now includes latitude and longitude
    data class Result(
        val name: String,
        val country: String,
        val admin1: String,
        val lat: Double,
        val lon: Double
    )

    fun search(query: String): List<Result> {
        if (query.isBlank() || query.length < 2) return emptyList()
        return try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?" +
                "name=" + URLEncoder.encode(query, "UTF-8") +
                "&count=20&language=en&format=json"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            if (conn.responseCode != 200) { conn.disconnect(); return emptyList() }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(body)
            val results = root.optJSONArray("results") ?: return emptyList()
            val out = mutableListOf<Result>()
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val name = item.optString("name", "")
                val country = item.optString("country", "")
                val admin1 = item.optString("admin1", "")
                val lat = item.optDouble("latitude", 0.0)
                val lon = item.optDouble("longitude", 0.0)
                if (name.isNotBlank() && country.isNotBlank() && lat != 0.0 && lon != 0.0) {
                    out.add(Result(name, country, admin1, lat, lon))
                }
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }
}
