package com.jannahos

object QuranSemanticSearch {
    // Maps human scenarios to specific Surah and Ayah
    private val semanticMap = mapOf(
        "musa" to listOf(18, 20, 28), // Al-Kahf, Taha, Al-Qasas
        "khidr" to listOf(18), // Al-Kahf
        "pharaoh" to listOf(10, 20, 26, 28, 79), // Yunus, Taha, Ash-Shu'ara, Al-Qasas, An-Nazi'at
        "firaun" to listOf(10, 20, 26, 28, 79),
        "crossing the river" to listOf(2, 20), // Al-Baqarah, Taha (splitting the sea)
        "splitting the sea" to listOf(26), // Ash-Shu'ara
        "bani israel" to listOf(2, 5, 7, 10, 17, 20), 
        "children of israel" to listOf(2, 5, 7, 10, 17, 20),
        "yusuf" to listOf(12), // Yusuf
        "joseph" to listOf(12),
        "maryam" to listOf(19), // Maryam
        "mary" to listOf(19),
        "isa" to listOf(3, 4, 5, 19), // Al-Imran, An-Nisa, Al-Ma'idah, Maryam
        "jesus" to listOf(3, 4, 5, 19),
        "sulaiman" to listOf(27, 34), // An-Naml, Saba
        "solomon" to listOf(27, 34),
        "yunus" to listOf(10, 37, 68), // Yunus, As-Saffat, Al-Qalam
        "jonah" to listOf(10, 37, 68),
        "ayyub" to listOf(21, 38), // Al-Anbiya, Sad
        "job" to listOf(21, 38),
        "patience" to listOf(2, 3, 103), // Al-Baqarah, Al-Imran, Al-Asr
        "sabr" to listOf(2, 3, 103),
        "anxiety" to listOf(13, 94), // Ar-Ra'd, Ash-Sharh
        "stress" to listOf(13, 94),
        "forgiveness" to listOf(39, 71), // Az-Zumar, Nuh
        "tawbah" to listOf(9),
        "paradise" to listOf(55, 56, 76), // Ar-Rahman, Al-Waqi'ah, Al-Insan
        "jannah" to listOf(55, 56, 76),
        "hellfire" to listOf(67, 78), // Al-Mulk, An-Naba
        "jahannam" to listOf(67, 78)
    )

    fun getSurahSuggestions(query: String): List<Int> {
        val q = query.lowercase()
        val results = mutableSetOf<Int>()
        
        // Check exact semantic matches
        for ((key, surahs) in semanticMap) {
            if (q.contains(key)) {
                results.addAll(surahs)
            }
        }
        
        // Fallback to matching the surah names directly
        for (surah in QuranCache.SURAHS) {
            if (surah.second.lowercase().contains(q)) {
                results.add(surah.first)
            }
        }
        
        return results.toList().sorted()
    }
}
