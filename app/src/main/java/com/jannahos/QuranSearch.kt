package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Quran search via alquran.cloud's public search endpoint.
// Free, no auth. Returns matched ayahs with surah:ayah and language.
object QuranSearch {

    data class Hit(
        val surah: Int,
        val ayah: Int,
        val arabic: String,
        val english: String,
        val surahName: String
    )

    fun search(query: String): List<Hit> {
        if (query.isBlank()) return emptyList()

        // Search both editions in parallel and merge by surah:ayah.
        val englishHits = try { searchEdition(query, "en.sahih") } catch (e: Exception) { emptyList() }
        val arabicHits  = try { searchEdition(query, "quran-uthmani") } catch (e: Exception) { emptyList() }

        val merged = LinkedHashMap<String, Hit>()
        englishHits.forEach { merged["${it.surah}:${it.ayah}"] = it }
        arabicHits.forEach { merged.putIfAbsent("${it.surah}:${it.ayah}", it) }
        return merged.values.toList()
    }

    private fun searchEdition(query: String, edition: String): List<Hit> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://api.alquran.cloud/v1/search/$encoded/all/$edition"
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("User-Agent", "JannahOS/1.0")
        conn.setRequestProperty("Accept", "application/json")

        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()

        val root = JSONObject(body)
        val data = root.optJSONObject("data") ?: return emptyList()
        val matches = data.optJSONArray("matches") ?: return emptyList()

        val out = mutableListOf<Hit>()
        for (i in 0 until matches.length()) {
            val m = matches.getJSONObject(i)
            val surahObj = m.optJSONObject("surah")
            val surahNum = surahObj?.optInt("number", 0) ?: 0
            val surahName = surahObj?.optString("englishName", "") ?: ""
            val ayahNum = m.optInt("numberInSurah", 0)
            val text = m.optString("text", "")
            val isArabic = edition.contains("uthmani")
            out.add(
                Hit(
                    surah = surahNum,
                    ayah = ayahNum,
                    arabic = if (isArabic) text else "",
                    english = if (!isArabic) text else "",
                    surahName = surahName
                )
            )
        }
        return out.take(60)
    }
}
