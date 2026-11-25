package com.jannahos.siraj.knowledge

import android.content.Context
import com.jannahos.QuranSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local Quran retrieval. Returns Arabic source text with canonical references.
 * The legacy English field is intentionally not emitted because its translator
 * and edition are not identified in the current asset metadata.
 */
object QuranEvidenceRetriever {
    suspend fun search(context: Context, query: String): List<EvidenceExcerpt> {
        if (query.isBlank()) return emptyList()
        val hits = withContext(Dispatchers.IO) {
            QuranSearchEngine.searchQuran(context.applicationContext, query.trim())
        }
        return hits.mapNotNull { hit ->
            fromArabicAyah(hit.surah, hit.ayah, hit.arabic)
        }
    }

    fun fromArabicAyah(
        surah: Int,
        ayah: Int,
        arabicText: String
    ): EvidenceExcerpt? {
        if (surah !in 1..114 || ayah < 1 || arabicText.isBlank()) return null

        return EvidenceExcerpt(
            reference = IslamicReference.Quran(
                sourceId = "quran-uthmani",
                surah = surah,
                ayah = ayah
            ),
            text = arabicText.trim(),
            language = "ar"
        )
    }
}
