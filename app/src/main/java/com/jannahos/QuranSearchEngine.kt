package com.jannahos

object QuranSearchEngine {
    private val semanticMap = mapOf(
        "musa" to listOf(18, 20, 28), // Al-Kahf, Taha, Al-Qasas
        "khidr" to listOf(18), // Al-Kahf
        "pharaoh" to listOf(10, 20, 26, 28, 79),
        "firaun" to listOf(10, 20, 26, 28, 79),
        "crossing the river" to listOf(2, 20),
        "splitting the sea" to listOf(26),
        "bani israel" to listOf(2, 5, 7, 10, 17, 20),
        "children of israel" to listOf(2, 5, 7, 10, 17, 20),
        "yusuf" to listOf(12), "joseph" to listOf(12),
        "maryam" to listOf(19), "mary" to listOf(19),
        "isa" to listOf(3, 4, 5, 19), "jesus" to listOf(3, 4, 5, 19),
        "sulaiman" to listOf(27, 34), "solomon" to listOf(27, 34),
        "yunus" to listOf(10, 37, 68), "jonah" to listOf(10, 37, 68),
        "ayyub" to listOf(21, 38), "job" to listOf(21, 38),
        "patience" to listOf(2, 3, 103), "sabr" to listOf(2, 3, 103),
        "anxiety" to listOf(13, 94), "stress" to listOf(13, 94),
        "forgiveness" to listOf(39, 71), "tawbah" to listOf(9),
        "paradise" to listOf(55, 56, 76), "jannah" to listOf(55, 56, 76),
        "hellfire" to listOf(67, 78), "jahannam" to listOf(67, 78)
    )

    fun search(query: String): List<Pair<Int, String>> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        
        val results = mutableSetOf<Int>()
        
        // 1. Semantic Matches
        for ((key, surahs) in semanticMap) {
            if (q.contains(key)) {
                results.addAll(surahs)
            }
        }
        
        // 2. Direct Surah Name Matches
        for (surah in QuranCache.SURAHS) {
            if (surah.second.lowercase().contains(q)) {
                results.add(surah.first)
            }
        }
        
        // Map IDs back to Names
        return results.toList().sorted().mapNotNull { id ->
            QuranCache.SURAHS.firstOrNull { it.first == id }
        }
    }
}
