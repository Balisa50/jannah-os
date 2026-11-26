package com.jannahos.siraj.knowledge

import com.jannahos.TafsirApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retrieves the English Tafsir Ibn Kathir excerpt for one exact ayah.
 *
 * Quran.com identifies resource 169 as Tafsir Ibn Kathir by Hafiz Ibn Kathir.
 * The current integration does not expose an English translator or edition,
 * so those fields are deliberately left unset.
 */
object TafsirEvidenceRetriever {
    private const val SOURCE_ID = "quran.com-tafsir-resource-169"

    suspend fun getAyah(surah: Int, ayah: Int): EvidenceExcerpt? =
        withContext(Dispatchers.IO) {
            if (surah !in 1..114 || ayah < 1) return@withContext null
            val text = TafsirApi.fetch(surah, ayah) ?: return@withContext null
            fromEnglishText(surah, ayah, text)
        }

    fun fromEnglishText(
        surah: Int,
        ayah: Int,
        text: String
    ): EvidenceExcerpt? {
        if (surah !in 1..114 || ayah < 1 || text.isBlank()) return null
        return EvidenceExcerpt(
            reference = IslamicReference.ScholarlyWork(
                sourceId = SOURCE_ID,
                author = "Hafiz Ibn Kathir",
                title = "Tafsir Ibn Kathir"
            ),
            text = text.trim(),
            language = "en"
        )
    }
}
