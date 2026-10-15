package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tafsir via quran.com. Their API returns the tafsir text under data.tafsir.text.
// Edition 169 = Ibn Kathir (abridged, English).
object TafsirApi {

    private const val TAFSIR_ID = 169
    private const val BASE = "https://api.quran.com/api/v4/tafsirs"

    fun fetch(surah: Int, ayah: Int): String? {
        val url = "$BASE/$TAFSIR_ID/by_ayah/$surah:$ayah"
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val tafsir = root.optJSONObject("tafsir") ?: return null
            tafsir.optString("text", null)
        } catch (e: Exception) {
            null
        }
    }
}
