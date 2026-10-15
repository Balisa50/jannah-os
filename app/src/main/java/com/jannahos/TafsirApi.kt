package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tafsir lookup for a single ayah. Uses Quran Cloud's free tafsir edition.
object TafsirApi {

    // Ibn Kathir abridged — the identification is `en.ibn-kathir` on this service.
    private const val EDITION = "en.ibn-kathir"

    fun fetch(surah: Int, ayah: Int): String? {
        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/$EDITION"
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val data = root.getJSONObject("data")
            data.getString("text")
        } catch (e: Exception) {
            null
        }
    }
}
