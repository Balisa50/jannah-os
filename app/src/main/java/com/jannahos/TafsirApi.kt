package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tafsir via Quran.com API: Tafsir Ibn Kathir (English), resource ID 169.
object TafsirApi {

    private const val TAFSIR_ID = 169
    private const val BASE = "https://api.quran.com/api/v4/tafsirs"

    fun fetch(surah: Int, ayah: Int): String? {
        val url = "$BASE/$TAFSIR_ID/by_ayah/$surah:$ayah"
        var connection: HttpURLConnection? = null
        return try {
            val activeConnection = URL(url).openConnection() as HttpURLConnection
            connection = activeConnection
            activeConnection.requestMethod = "GET"
            activeConnection.connectTimeout = 15000
            activeConnection.readTimeout = 15000
            activeConnection.setRequestProperty("User-Agent", "JannahOS/1.0")
            activeConnection.setRequestProperty("Accept", "application/json")
            if (activeConnection.responseCode !in 200..299) return null
            val body = activeConnection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val tafsir = root.optJSONObject("tafsir") ?: return null
            val raw = tafsir.optString("text", null) ?: return null
            stripHtml(raw)
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
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
