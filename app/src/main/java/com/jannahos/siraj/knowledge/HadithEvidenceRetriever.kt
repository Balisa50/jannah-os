package com.jannahos.siraj.knowledge

import android.content.Context
import com.jannahos.HadithStore
import com.jannahos.QuranSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retrieves raw Arabic hadith excerpts from the bundled merged dataset.
 *
 * The asset does not provide verified edition or grading metadata. This
 * retriever therefore never invents a grade, grader, edition or chain.
 */
object HadithEvidenceRetriever {
    suspend fun search(context: Context, query: String): List<EvidenceExcerpt> {
        if (query.isBlank()) return emptyList()
        val hits = withContext(Dispatchers.IO) {
            QuranSearchEngine.searchHadith(context.applicationContext, query.trim())
        }
        return hits.mapNotNull { hit ->
            fromBundledRecord(
                collection = hit.collection,
                number = hit.number,
                arabic = hit.arabic
            )
        }
    }

    fun fromBundledRecord(
        collection: String,
        number: Int,
        arabic: String
    ): EvidenceExcerpt? {
        if (collection.isBlank() || number <= 0 || arabic.isBlank()) return null
        return EvidenceExcerpt(
            reference = IslamicReference.Hadith(
                sourceId = "bundled-hadiths-merged-unattributed",
                collection = collection.trim(),
                referenceNumber = number.toString(),
                edition = null,
                grade = null,
                gradingAuthority = null
            ),
            text = arabic.trim(),
            language = "ar"
        )
    }
}
