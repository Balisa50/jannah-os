package com.jannahos.siraj.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TafsirEvidenceRetrieverTest {
    @Test
    fun identifiesTheTafsirResourceWithoutInventingEditionOrTranslator() {
        val tafsir = TafsirEvidenceRetriever.fromEnglishText(
            surah = 2,
            ayah = 255,
            text = "An excerpt from the tafsir."
        )!!

        val reference = tafsir.reference as IslamicReference.Tafsir
        assertEquals("quran.com-tafsir-resource-169", reference.sourceId)
        assertEquals("Hafiz Ibn Kathir", reference.author)
        assertEquals("Tafsir Ibn Kathir", reference.title)
        assertEquals(2, reference.surah)
        assertEquals(255, reference.ayah)
        assertEquals(null, reference.edition)
        assertEquals(null, reference.translator)
        assertEquals("en", tafsir.language)
    }

    @Test
    fun tafsirAndExactArabicAyahPassTheQuranExplanationEvidenceGate() {
        val quran = QuranEvidenceRetriever.fromArabicAyah(2, 255, "Arabic ayah")!!
        val tafsir = TafsirEvidenceRetriever.fromEnglishText(
            2,
            255,
            "Tafsir excerpt for the same ayah."
        )!!

        val review = EvidencePolicy.review(
            ClaimCategory.QURAN_EXPLANATION,
            listOf(quran, tafsir)
        )

        assertEquals(EvidenceDecision.ACCEPTED, review.decision)
        assertTrue(review.reasons.isEmpty())
    }

    @Test
    fun evidenceGateRejectsTafsirForADifferentAyah() {
        val quran = QuranEvidenceRetriever.fromArabicAyah(2, 255, "Arabic ayah")!!
        val tafsir = TafsirEvidenceRetriever.fromEnglishText(
            2,
            256,
            "Tafsir excerpt for a different ayah."
        )!!

        val review = EvidencePolicy.review(
            ClaimCategory.QURAN_EXPLANATION,
            listOf(quran, tafsir)
        )

        assertEquals(EvidenceDecision.BLOCKED, review.decision)
        assertTrue(review.reasons.any { it.contains("same Surah and ayah") })
    }

    @Test
    fun rejectsInvalidOrEmptyTafsirRecords() {
        assertNull(TafsirEvidenceRetriever.fromEnglishText(0, 1, "text"))
        assertNull(TafsirEvidenceRetriever.fromEnglishText(115, 1, "text"))
        assertNull(TafsirEvidenceRetriever.fromEnglishText(2, 0, "text"))
        assertNull(TafsirEvidenceRetriever.fromEnglishText(2, 255, " "))
    }
}
