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

        // The alquran.cloud API prefixes ayah 1 of most surahs with the basmalah.
        // We strip it here so the reader can render the basmalah as its own header.
        val stripBasmalah = surahNumber != 1 && surahNumber != 9
        return arabicAyahs.mapIndexed { i, a ->
            val arabic = if (stripBasmalah && a.number == 1)
                stripBasmalahPrefix(a.arabic) else a.arabic
            Ayah(a.number, arabic, englishAyahs.getOrNull(i)?.arabic ?: "", a.globalNumber)
        }
    }

    data class Ayah(
        val number: Int,
        val arabic: String,
        val english: String,
        val globalNumber: Int = 0
    )

    private fun parseAyahs(json: String): List<Ayah> {
        val root = JSONObject(json)
        val data = root.getJSONObject("data")
        val array = data.getJSONArray("ayahs")
        val out = mutableListOf<Ayah>()
        for (i in 0 until array.length()) {
            val a = array.getJSONObject(i)
            out.add(
                Ayah(
                    number = a.getInt("numberInSurah"),
                    arabic = a.getString("text"),
                    english = "",
                    globalNumber = a.optInt("number", 0)
                )
            )
        }
        return out
    }

    // The Uthmani basmalah as returned by the API. We strip this prefix.
    private val BASMALAH_PREFIXES = listOf(
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"
    )

    private fun stripBasmalahPrefix(text: String): String {
        var t = text
        for (p in BASMALAH_PREFIXES) {
            if (t.startsWith(p)) {
                t = t.removePrefix(p).trimStart()
                break
            }
        }
        return t
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
