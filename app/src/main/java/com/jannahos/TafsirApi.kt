package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tafsir via quran.com API. Ibn Kathir (abridged, English), edition 169.
object TafsirApi {

    private const val TAFSIR_ID = 169
    private const val BASE = "https://api.quran.com/api/v4/tafsirs"

    fun fetch(surah: Int, ayah: Int): String? {
        val url = "$BASE/$TAFSIR_ID/by_ayah/$surah:$ayah"
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val tafsir = root.optJSONObject("tafsir") ?: return null
            val raw = tafsir.optString("text", null) ?: return null
            stripHtml(raw)
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    // quran.com returns tafsir as HTML. We render plain text.
    private fun stripHtml(input: String): String {
        var s = input
        s = s.replace(Regex("(?i)<br\\s*/?>"), "\n")
        s = s.replace(Regex("(?i)</p>"), "\n\n")
        s = s.replace(Regex("(?i)</h[1-6]>"), "\n\n")
        s = s.replace(Regex("<[^>]+>"), "")
        s = s.replace("&nbsp;", " ")
        s = s.replace("&amp;", "&")
        s = s.replace("&lt;", "<")
        s = s.replace("&gt;", ">")
        s = s.replace("&quot;", "\"")
        s = s.replace("&#39;", "'")
        s = s.replace(Regex("\n{3,}"), "\n\n")
        return s.trim()
    }
}
