package com.jannahos.siraj.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuranEvidenceRetrieverTest {
    @Test
    fun mapsArabicAyahToExactCanonicalReference() {
        val evidence = QuranEvidenceRetriever.fromArabicAyah(
            surah = 18,
            ayah = 10,
            arabicText = "رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً"
        )

        assertEquals(18, (evidence?.reference as IslamicReference.Quran).surah)
        assertEquals(10, (evidence.reference as IslamicReference.Quran).ayah)
        assertEquals("ar", evidence.language)
    }

    @Test
    fun rejectsInvalidOrEmptyAyahData() {
        assertNull(QuranEvidenceRetriever.fromArabicAyah(115, 1, "text"))
        assertNull(QuranEvidenceRetriever.fromArabicAyah(18, 0, "text"))
        assertNull(QuranEvidenceRetriever.fromArabicAyah(18, 1, " "))
    }

    @Test
    fun returnedArabicEvidencePassesReferenceIntegrityButNotInterpretiveGateAlone() {
        val evidence = QuranEvidenceRetriever.fromArabicAyah(18, 10, "Arabic text")!!

        val referenceCheck = EvidencePolicy.review(
            ClaimCategory.GENERAL_INFORMATION,
            listOf(evidence)
        )
        val explanationCheck = EvidencePolicy.review(
            ClaimCategory.QURAN_EXPLANATION,
            listOf(evidence)
        )

        assertEquals(EvidenceDecision.ACCEPTED, referenceCheck.decision)
        assertEquals(EvidenceDecision.BLOCKED, explanationCheck.decision)
    }
}
