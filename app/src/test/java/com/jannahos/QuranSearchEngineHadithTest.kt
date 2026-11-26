package com.jannahos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranSearchEngineHadithTest {
    @Test fun exactEnglishMatchScoresHighest() {
        assertEquals(10, QuranSearchEngine.scoreHadithRecord("patience", "Patience is a virtue", ""))
    }

    @Test fun arabicMatchUsesNormalizedText() {
        val score = QuranSearchEngine.scoreHadithRecord("الرحمة", "", "الرَّحْمَةُ")
        assertTrue(score > 0)
    }

    @Test fun prophetSynonymIsRecognized() {
        assertEquals(5, QuranSearchEngine.scoreHadithRecord("moses", "Musa عليه السلام", ""))
    }

    @Test fun unrelatedRecordDoesNotScore() {
        assertEquals(0, QuranSearchEngine.scoreHadithRecord("patience", "A report about trade", ""))
    }
}
