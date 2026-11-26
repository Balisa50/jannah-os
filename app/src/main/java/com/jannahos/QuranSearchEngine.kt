package com.jannahos

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import java.util.PriorityQueue

object QuranSearchEngine {
    data class AyahResult(val surah: Int, val ayah: Int, val arabic: String, val english: String, val score: Int)
    data class HadithResult(val collection: String, val number: Int, val arabic: String, val english: String, val score: Int)

    private val synonyms = mapOf(
        "moses" to "musa", "jesus" to "isa", "mary" to "maryam",
        "jonah" to "yunus", "pharaoh" to "firaun", "solomon" to "sulaiman",
        "job" to "ayyub", "joseph" to "yusuf", "abraham" to "ibrahim",
        "noah" to "nuh", "adam" to "adam", "david" to "dawud",
        "cross" to "crossed", "river" to "river", "sea" to "sea"
    )

    fun searchQuran(ctx: Context, query: String): List<AyahResult> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        val arabicQuery = ArabicTextNormalizer.normalize(query)
        val tokens = q.split(Regex("\\s+")).filter { it.isNotBlank() }
        val arabicTokens = arabicQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        val results = mutableListOf<AyahResult>()
        val allAyahs = QuranTextStore.load(ctx)

        for (ayah in allAyahs) {
            var score = 0
            val engLower = ayah.english.lowercase()
            val araText = ArabicTextNormalizer.normalize(ayah.arabic)

            // 1. Exact Matches (Fast)
            for (token in tokens) {
                if (engLower.contains(token)) score += 10
            }
            for (token in arabicTokens) {
                if (token.isNotBlank() && araText.contains(token)) score += 10
            }

            // 2. Synonym Matches
            for (token in tokens) {
                val syn = synonyms[token]
                if (syn != null && (engLower.contains(syn) || araText.contains(syn))) score += 5
            }

            // 3. Fuzzy Match (Only if exact match failed)
            if (score == 0) {
                for (token in tokens) {
                    if (token.length > 3) {
                        val words = engLower.split(Regex("\\W+"))
                        for (w in words) {
                            if (w.length > 3 && levenshtein(token, w) <= 1) {
                                score += 2
                                break
                            }
                        }
                    }
                }
            }

            if (score > 0) results.add(AyahResult(ayah.surah, ayah.ayah, ayah.arabic, ayah.english, score))
        }
        return results.sortedByDescending { it.score }.take(50)
    }

    /**
     * Stream the large bundled dataset instead of materialising the entire
     * 50 MB JSON file and every record in memory. Keep only the best 50 hits.
     */
    fun searchHadith(ctx: Context, query: String): List<HadithResult> {
        if (query.isBlank()) return emptyList()
        val best = PriorityQueue<HadithResult>(50, compareBy { it.score })

        return try {
            ctx.applicationContext.assets.open("hadiths_merged.bin").use { compressed ->
                GZIPInputStream(compressed).use { input ->
                JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        var collection = ""
                        var number = 0
                        var arabic = ""
                        var english = ""
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "collection" -> collection = readJsonString(reader)
                                "number" -> number = readJsonInt(reader)
                                "arabic" -> arabic = readJsonString(reader)
                                "english" -> english = readJsonString(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (collection.isBlank() || number <= 0 || arabic.isBlank()) continue
                        val score = scoreHadithRecord(query, english, arabic)
                        if (score <= 0) continue
                        val hit = HadithResult(collection, number, arabic, english, score)
                        if (best.size < 50) {
                            best.add(hit)
                        } else if (score > (best.peek()?.score ?: Int.MIN_VALUE)) {
                            best.poll()
                            best.add(hit)
                        }
                    }
                    reader.endArray()
                }
                }
            }
            best.sortedByDescending { it.score }
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun scoreHadithRecord(query: String, english: String, arabic: String): Int {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return 0
        val tokens = q.split(Regex("\\s+")).filter { it.isNotBlank() }
        val arabicTokens = ArabicTextNormalizer.normalize(query)
            .split(Regex("\\s+")).filter { it.isNotBlank() }
        val engLower = english.lowercase()
        val araText = ArabicTextNormalizer.normalize(arabic)
        var score = 0

        for (token in tokens) if (engLower.contains(token)) score += 10
        for (token in arabicTokens) if (araText.contains(token)) score += 10
        for (token in tokens) {
            val synonym = synonyms[token]
            if (synonym != null && engLower.contains(synonym)) score += 5
        }

        if (score == 0) {
            for (token in tokens) {
                if (token.length > 4) {
                    for (word in engLower.split(Regex("\\W+"))) {
                        if (word.length > 4 && levenshtein(token, word) <= 1) {
                            score += 2
                            break
                        }
                    }
                }
            }
        }
        return score
    }

    private fun readJsonString(reader: JsonReader): String = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            ""
        }
        JsonToken.STRING, JsonToken.NUMBER -> reader.nextString()
        else -> {
            reader.skipValue()
            ""
        }
    }

    private fun readJsonInt(reader: JsonReader): Int = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            0
        }
        JsonToken.NUMBER, JsonToken.STRING -> reader.nextString().toIntOrNull() ?: 0
        else -> {
            reader.skipValue()
            0
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }
}
