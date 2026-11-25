package com.jannahos.siraj.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithEvidenceRetrieverTest {
    @Test
    fun createsRawArabicEvidenceWithoutInventingEditionOrGrade() {
        val excerpt = HadithEvidenceRetriever.fromBundledRecord(
            collection = "Sahih al-Bukhari",
            number = 1,
            arabic = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ"
        )
        assertNotNull(excerpt)
        val reference = excerpt!!.reference as IslamicReference.Hadith
        assertEquals("bundled-hadiths-merged-unattributed", reference.sourceId)
        assertEquals("Sahih al-Bukhari", reference.collection)
        assertEquals("1", reference.referenceNumber)
        assertNull(reference.edition)
        assertNull(reference.grade)
        assertNull(reference.gradingAuthority)
        assertEquals("ar", excerpt.language)
    }

    @Test
    fun blocksAuthenticityVerdictWhenBundledRecordHasNoGradingProvenance() {
        val excerpt = HadithEvidenceRetriever.fromBundledRecord(
            collection = "Sahih al-Bukhari",
            number = 1,
            arabic = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ"
        )!!
        val review = EvidencePolicy.review(
            ClaimCategory.HADITH_AUTHENTICITY,
            listOf(excerpt)
        )
        assertEquals(EvidenceDecision.BLOCKED, review.decision)
        assertTrue(review.reasons.any { it.contains("grading") })
    }

    @Test
    fun rejectsIncompleteBundledRecords() {
        assertNull(HadithEvidenceRetriever.fromBundledRecord("", 1, "Arabic"))
        assertNull(HadithEvidenceRetriever.fromBundledRecord("Collection", 0, "Arabic"))
        assertNull(HadithEvidenceRetriever.fromBundledRecord("Collection", 1, ""))
    }
}
