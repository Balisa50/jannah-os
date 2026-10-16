package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Word-level morphology via quran.com. Returns per-word Arabic, transliteration,
// English meaning, and Arabic root letters when available.
object WordByWordApi {

    data class Word(
        val position: Int,
        val arabic: String,
        val transliteration: String,
        val meaning: String,
        val root: String?,
        val grammar: String?
    )

    // Cache in memory per surah. Not persisted, reloads on app restart.
    private val cache = mutableMapOf<Int, List<List<Word>>>()

    fun cached(surah: Int): List<List<Word>>? = cache[surah]

    fun fetch(surah: Int): List<List<Word>> {
        cache[surah]?.let { return it }

        val wordsForSurah = mutableListOf<List<Word>>()
        var page = 1
        while (true) {
            val url = "https://api.quran.com/api/v4/verses/by_chapter/$surah" +
                "?words=true&word_fields=text_uthmani,transliteration,root_letters" +
                "&fields=text_uthmani&per_page=50&page=$page"

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(body)
            val verses = root.getJSONArray("verses")
            if (verses.length() == 0) break

            for (i in 0 until verses.length()) {
                val v = verses.getJSONObject(i)
                val wordsArr = v.optJSONArray("words") ?: continue
                val words = mutableListOf<Word>()
                for (j in 0 until wordsArr.length()) {
                    val w = wordsArr.getJSONObject(j)
                    // Skip end-of-ayah markers (word char type). We detect by text.
                    val arabic = w.optString("text_uthmani", "")
                    if (arabic.isBlank()) continue

                    val translit = w.optString("transliteration", "")
                        .let { stripTags(it) }
                    val meaning = w.optJSONObject("translation")?.optString("text", "")
                        ?.let { stripTags(it) } ?: ""
                    val root = w.optString("root_letters", "").ifBlank { null }
                    val position = w.optInt("position", j + 1)

                    words.add(Word(position, arabic, translit, meaning, root, null))
                }
                wordsForSurah.add(words)
            }

            val meta = root.optJSONObject("pagination")
            val nextPage = meta?.optString("next_page")
            if (nextPage.isNullOrBlank() || nextPage == "null") break
            page = nextPage.toIntOrNull() ?: break
            if (page <= 0) break
        }

        cache[surah] = wordsForSurah
        return wordsForSurah
    }

    // The API may include <em>...</em> highlight tags inside transliteration/meaning.
    private fun stripTags(s: String): String =
        s.replace(Regex("<[^>]+>"), "").trim()
}
