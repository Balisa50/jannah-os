package com.jannahos.siraj.ai

/**
 * Conservative local gate: general-purpose models are not used to generate
 * Islamic rulings, scripture interpretation or authenticity claims.
 * This is a safety boundary, not a semantic classifier.
 */
object SirajConversationPolicy {
    private val religiousIntentPhrases = listOf(
        "islamic ruling", "fiqh", "fatwa", "hadith", "quran", "surah", "ayah",
        "tafsir", "sunnah", "halal", "haram", "is it permissible", "is it allowed in islam",
        "is this permissible", "is this allowed in islam", "prayer time", "prayer times",
        "salah", "salat", "wudu", "wudhu", "ghusl", "zakat", "ramadan", "fasting",
        "is it sinful", "is this a sin", "prophet muhammad", "the prophet", "allah",
        "dua", "du'a", "adhkar", "islam"
    )

    fun isReligiousRequest(input: String): Boolean {
        val normalized = input.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}' ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return religiousIntentPhrases.any { phrase ->
            normalized == phrase || normalized.contains(phrase)
        }
    }

    const val GENERAL_ASSISTANT_SYSTEM_PROMPT =
        "You are SIRAJ, a concise and useful general-purpose assistant. " +
        "Help with ordinary learning, writing, coding, planning and everyday questions. " +
        "Do not provide Islamic rulings, interpret Quran or Hadith, assess hadith authenticity, " +
        "or present yourself as a religious authority. If a user asks for religious guidance, " +
        "say SIRAJ's verified Islamic research workflow is not available yet. " +
        "Never claim to have taken an action on the user's device."
}
