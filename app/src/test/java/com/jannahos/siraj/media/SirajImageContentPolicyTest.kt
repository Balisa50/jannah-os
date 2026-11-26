package com.jannahos.siraj.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajImageContentPolicyTest {
    @Test fun blocksArabicScriptFromGeneralProvider() {
        assertTrue(SirajImageContentPolicy.requiresIslamicReview("هذا نص عربي"))
    }

    @Test fun blocksRecognizedIslamicReferences() {
        assertTrue(SirajImageContentPolicy.requiresIslamicReview("A page containing a hadith"))
        assertTrue(SirajImageContentPolicy.requiresIslamicReview("Qur'an translation"))
    }

    @Test fun allowsOrdinaryNonReligiousText() {
        assertFalse(SirajImageContentPolicy.requiresIslamicReview("Invoice total: 250.00 GHS"))
    }
}
