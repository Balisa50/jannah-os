package com.jannahos.siraj.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidencePolicyTest {
    @Test
    fun blocksAnswersWithoutEvidence() {
        val result = EvidencePolicy.review(ClaimCategory.GENERAL_INFORMATION, emptyList())
        assertEquals(EvidenceDecision.BLOCKED, result.decision)
        assertTrue(result.reasons.isNotEmpty())
    }

    @Test
    fun requiresExactQuranReferenceAndIdentifiedTranslation() {
        val quran = EvidenceExcerpt(
            reference = IslamicReference.Quran(
                sourceId = "quran-uthmani",
                surah = 18,
                ayah = 10,
                translationId = "translator-id",
                translationEdition = "edition-1"
            ),
            text = "A cited translation excerpt",
            language = "en"
        )
        val tafsir = EvidenceExcerpt(
            reference = IslamicReference.ScholarlyWork(
                sourceId = "tafsir-source",
                author = "Named author",
                title = "Named tafsir"
            ),
            text = "A cited interpretive excerpt",
            language = "en"
        )

        val result = EvidencePolicy.review(
            ClaimCategory.QURAN_EXPLANATION,
            listOf(quran, tafsir)
        )
        assertEquals(EvidenceDecision.ACCEPTED, result.decision)
    }

    @Test
    fun blocksHadithAuthenticityWhenGradeOrGraderIsMissing() {
        val hadith = EvidenceExcerpt(
            reference = IslamicReference.Hadith(
                sourceId = "dataset-record",
                collection = "Collection",
                referenceNumber = "123"
            ),
            text = "Hadith excerpt",
            language = "en"
        )

        val result = EvidencePolicy.review(
            ClaimCategory.HADITH_AUTHENTICITY,
            listOf(hadith)
        )
        assertEquals(EvidenceDecision.BLOCKED, result.decision)
        assertTrue(result.reasons.any { it.contains("grading") })
    }

    @Test
    fun acceptsHadithAuthenticityOnlyWithExplicitGradingProvenance() {
        val hadith = EvidenceExcerpt(
            reference = IslamicReference.Hadith(
                sourceId = "edition-record",
                collection = "Collection",
                referenceNumber = "123",
                edition = "Edition",
                grade = "Grade supplied by source",
                gradingAuthority = "Named grading authority"
            ),
            text = "Hadith excerpt",
            language = "en"
        )

        val result = EvidencePolicy.review(
            ClaimCategory.HADITH_AUTHENTICITY,
            listOf(hadith)
        )
        assertEquals(EvidenceDecision.ACCEPTED, result.decision)
    }

    @Test
    fun blocksFiqhRulingWithoutTraceableScholarlyPageReference() {
        val source = EvidenceExcerpt(
            reference = IslamicReference.ScholarlyWork(
                sourceId = "book-record",
                author = "Named author",
                title = "Named work"
            ),
            text = "A cited scholarly excerpt",
            language = "en"
        )

        val result = EvidencePolicy.review(ClaimCategory.FIQH_RULING, listOf(source))
        assertEquals(EvidenceDecision.BLOCKED, result.decision)
        assertTrue(result.reasons.any { it.contains("volume and page") })
    }

    @Test
    fun rejectsInvalidQuranReferences() {
        val source = EvidenceExcerpt(
            reference = IslamicReference.Quran(
                sourceId = "quran-uthmani",
                surah = 115,
                ayah = 0
            ),
            text = "Invalid reference",
            language = "ar"
        )

        val result = EvidencePolicy.review(ClaimCategory.GENERAL_INFORMATION, listOf(source))
        assertEquals(EvidenceDecision.BLOCKED, result.decision)
    }

    @Test
    fun blocksAuthenticityClaimIfAnyCitedHadithLacksGrading() {
        val graded = EvidenceExcerpt(
            IslamicReference.Hadith(
                sourceId = "record-a",
                collection = "Collection",
                referenceNumber = "1",
                grade = "Grade",
                gradingAuthority = "Authority"
            ),
            "First excerpt",
            "en"
        )
        val ungraded = EvidenceExcerpt(
            IslamicReference.Hadith(
                sourceId = "record-b",
                collection = "Collection",
                referenceNumber = "2"
            ),
            "Second excerpt",
            "en"
        )

        val result = EvidencePolicy.review(
            ClaimCategory.HADITH_AUTHENTICITY,
            listOf(graded, ungraded)
        )
        assertEquals(EvidenceDecision.BLOCKED, result.decision)
    }
}
