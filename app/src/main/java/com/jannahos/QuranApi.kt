package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Talks to api.alquran.cloud — a free public service, no auth required.
// We cache every response in QuranCache so the app works offline after first read.
object QuranApi {

    private const val BASE = "https://api.alquran.cloud/v1"

    // Fetch one surah with two editions: Arabic (quran-uthmani) and English (en.sahih).
    // Returns a list of (ayahNumber, arabic, english) triples.
    fun fetchSurah(surahNumber: Int): List<Ayah> {
        val arabicJson = get("$BASE/surah/$surahNumber/quran-uthmani")
        val englishJson = get("$BASE/surah/$surahNumber/en.sahih")

        val arabicAyahs = parseAyahs(arabicJson)
        val englishAyahs = parseAyahs(englishJson)

        return arabicAyahs.mapIndexed { i, a ->
            Ayah(a.number, a.arabic, englishAyahs.getOrNull(i)?.arabic ?: "")
        }
    }

    data class Ayah(val number: Int, val arabic: String, val english: String)

    private fun parseAyahs(json: String): List<Ayah> {
        val root = JSONObject(json)
        val data = root.getJSONObject("data")
        val array = data.getJSONArray("ayahs")
        val out = mutableListOf<Ayah>()
        for (i in 0 until array.length()) {
            val a = array.getJSONObject(i)
            out.add(Ayah(a.getInt("numberInSurah"), a.getString("text"), ""))
        }
        return out
    }

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("User-Agent", "JannahOS/1.0")
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
