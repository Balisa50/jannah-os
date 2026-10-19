package com.jannahos

object QuranSearchEngine {
    data class SearchResult(val surahId: Int, val name: String, val page: Int)

    // Maps human scenarios directly to the exact page in the Madani Mushaf
    private val semanticMap = mapOf(
        "musa" to listOf(SearchResult(18, "Al-Kahf (Musa & Khidr)", 299)),
        "khidr" to listOf(SearchResult(18, "Al-Kahf (Musa & Khidr)", 299)),
        "pharaoh" to listOf(SearchResult(20, "Ta-Ha (Pharaoh)", 312)),
        "firaun" to listOf(SearchResult(20, "Ta-Ha (Pharaoh)", 312)),
        "crossing the river" to listOf(SearchResult(26, "Ash-Shu'ara (Splitting the Sea)", 367)),
        "splitting the sea" to listOf(SearchResult(26, "Ash-Shu'ara (Splitting the Sea)", 367)),
        "yusuf" to listOf(SearchResult(12, "Yusuf", 235)),
        "maryam" to listOf(SearchResult(19, "Maryam", 305)),
        "sulaiman" to listOf(SearchResult(27, "An-Naml (Solomon)", 377)),
        "yunus" to listOf(SearchResult(10, "Yunus", 208)),
        "ayyub" to listOf(SearchResult(21, "Al-Anbiya (Job)", 322)),
        "patience" to listOf(SearchResult(2, "Al-Baqarah", 2), SearchResult(103, "Al-Asr", 601)),
        "anxiety" to listOf(SearchResult(13, "Ar-Ra'd", 249), SearchResult(94, "Ash-Sharh", 596)),
        "forgiveness" to listOf(SearchResult(39, "Az-Zumar", 458)),
        "paradise" to listOf(SearchResult(55, "Ar-Rahman", 531))
    )

    fun search(query: String): List<SearchResult> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        
        val results = mutableSetOf<SearchResult>()
        
        // 1. Semantic Matches
        for ((key, surahs) in semanticMap) {
            if (q.contains(key)) {
                results.addAll(surahs)
            }
        }
        
        // 2. Direct Surah Name Matches
        for (surah in QuranCache.SURAHS) {
            if (surah.second.lowercase().contains(q)) {
                // Default to the first page of the surah
                results.add(SearchResult(surah.first, surah.second, QuranStore.firstPageOfSurah(surah.first)))
            }
        }
        
        return results.toList().sortedBy { it.page }
    }
}
