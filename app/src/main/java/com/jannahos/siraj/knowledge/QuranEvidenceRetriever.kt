package com.jannahos.siraj.knowledge

import android.content.Context
import com.jannahos.QuranSearchEngine
import com.jannahos.QuranTextStore
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

    suspend fun getAyah(
        context: Context,
        surah: Int,
        ayah: Int
    ): EvidenceExcerpt? = withContext(Dispatchers.IO) {
        if (surah !in 1..114 || ayah < 1) return@withContext null
        val stored = QuranTextStore.getAyah(context.applicationContext, surah, ayah)
            ?: return@withContext null
        fromArabicAyah(stored.surah, stored.ayah, stored.arabic)
    }

    fun fromArabicAyah(
        surah: Int,
        ayah: Int,
        arabicText: String
    ): EvidenceExcerpt? {
        if (surah !in 1..114 || ayah < 1 || arabicText.isBlank()) return null

        return EvidenceExcerpt(
            reference = IslamicReference.Quran(
                sourceId = "bundled-quran-arabic-unattributed",
                surah = surah,
                ayah = ayah
            ),
            text = arabicText.trim(),
            language = "ar"
        )
    }
}
