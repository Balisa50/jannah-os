package com.jannahos.siraj.media

/** Fail-closed gate before OCR text can be sent to a general-purpose model. */
object SirajImageContentPolicy {
    private val restrictedTerms = listOf(
        "quran", "qur'an", "hadith", "allah", "prophet muhammad",
        "salah", "surah", "ayah", "fiqh", "fatwa", "dua", "adhkar",
        "bismillah", "alhamdulillah", "islamic", "islam", "muslim",
        "muhammad", "prayer", "mosque", "ramadan", "fasting",
        "verse", "scripture", "quranic"
    )

    fun requiresIslamicReview(text: String): Boolean {
        if (text.any {
                Character.UnicodeScript.of(it.code) == Character.UnicodeScript.ARABIC
            }
        ) return true

        val normalized = " ${text.lowercase().replace(Regex("[^a-z0-9']+"), " ")} "
        return restrictedTerms.any { term -> normalized.contains(" $term ") }
    }
}
