package com.jannahos

import android.content.Context

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

    fun searchHadith(ctx: Context, query: String): List<HadithResult> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        val arabicQuery = ArabicTextNormalizer.normalize(query)
        val tokens = q.split(Regex("\\s+")).filter { it.isNotBlank() }
        val arabicTokens = arabicQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        val results = mutableListOf<HadithResult>()
        val allHadiths = HadithStore.load(ctx)

        for (h in allHadiths) {
            var score = 0
            val engLower = h.english.lowercase()
            val araText = ArabicTextNormalizer.normalize(h.arabic)

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
                if (syn != null && engLower.contains(syn)) score += 5
            }

            // 3. Fuzzy Match (Only if exact match failed)
            if (score == 0) {
                for (token in tokens) {
                    if (token.length > 4) {
                        val words = engLower.split(Regex("\\W+"))
                        for (w in words) {
                            if (w.length > 4 && levenshtein(token, w) <= 1) {
                                score += 2
                                break
                            }
                        }
                    }
                }
            }

            if (score > 0) results.add(HadithResult(h.collection, h.number, h.arabic, h.english, score))
        }
        return results.sortedByDescending { it.score }.take(50)
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
