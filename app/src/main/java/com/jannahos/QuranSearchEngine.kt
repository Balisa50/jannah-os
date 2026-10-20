package com.jannahos

object QuranSearchEngine {
    data class SearchResult(val surahId: Int, val name: String, val page: Int, val ayahStart: Int = 1, val ayahEnd: Int = 999)

    private val semanticMap = mapOf(
        "musa" to listOf(SearchResult(18, "Al-Kahf (Musa & Khidr)", 299, 60, 82)),
        "khidr" to listOf(SearchResult(18, "Al-Kahf (Musa & Khidr)", 299, 60, 82)),
        "pharaoh" to listOf(SearchResult(20, "Ta-Ha (Pharaoh)", 312, 24, 79)),
        "firaun" to listOf(SearchResult(20, "Ta-Ha (Pharaoh)", 312, 24, 79)),
        "crossing the river" to listOf(SearchResult(26, "Ash-Shu'ara (Splitting the Sea)", 367, 52, 68)),
        "splitting the sea" to listOf(SearchResult(26, "Ash-Shu'ara (Splitting the Sea)", 367, 52, 68)),
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

    fun search(ctx: android.content.Context, query: String): List<SearchResult> {
        // First try semantic
        val semantic = search(query)
        if (semantic.isNotEmpty()) return semantic
        
        // Then fallback to text search
        val textResults = QuranTextStore.search(ctx, query)
        return textResults.map { 
            SearchResult(it.surah, "Surah ${it.surah}", 1) 
        }.distinctBy { it.surahId }
    }

    fun search(query: String): List<SearchResult> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        
        val results = mutableSetOf<SearchResult>()
        for ((key, surahs) in semanticMap) {
            if (q.contains(key)) results.addAll(surahs)
        }
        for (surah in QuranCache.SURAHS) {
            if (surah.second.lowercase().contains(q)) {
                results.add(SearchResult(surah.first, surah.second, QuranStore.firstPageOfSurah(surah.first)))
            }
        }
        return results.toList().sortedBy { it.page }
    }
}
