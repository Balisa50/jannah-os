package com.jannahos.siraj.knowledge

/**
 * Explicit provenance for a source used in a SIRAJ response.
 *
 * References deliberately keep edition, translation and grading metadata
 * separate. A model must never manufacture missing bibliographic fields.
 */
sealed interface IslamicReference {
    val sourceId: String

    data class Quran(
        override val sourceId: String,
        val surah: Int,
        val ayah: Int,
        val translationId: String? = null,
        val translationEdition: String? = null
    ) : IslamicReference

    data class Hadith(
        override val sourceId: String,
        val collection: String,
        val referenceNumber: String,
        val edition: String? = null,
        val grade: String? = null,
        val gradingAuthority: String? = null
    ) : IslamicReference

    data class ScholarlyWork(
        override val sourceId: String,
        val author: String,
        val title: String,
        val edition: String? = null,
        val volume: String? = null,
        val page: String? = null,
        val publisher: String? = null
    ) : IslamicReference
}

data class EvidenceExcerpt(
    val reference: IslamicReference,
    val text: String,
    val language: String
)

enum class ClaimCategory {
    GENERAL_INFORMATION,
    QURAN_EXPLANATION,
    HADITH_AUTHENTICITY,
    FIQH_RULING
}

enum class EvidenceDecision {
    ACCEPTED,
    BLOCKED
}

data class EvidenceReview(
    val decision: EvidenceDecision,
    val reasons: List<String>
)
