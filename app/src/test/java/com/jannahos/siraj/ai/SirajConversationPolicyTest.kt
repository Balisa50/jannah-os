package com.jannahos.siraj.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajConversationPolicyTest {
    @Test
    fun blocksDirectIslamicQuestionsFromGeneralModel() {
        assertTrue(SirajConversationPolicy.isReligiousRequest("Is this halal?"))
        assertTrue(SirajConversationPolicy.isReligiousRequest("Explain Surah Al-Kahf"))
        assertTrue(SirajConversationPolicy.isReligiousRequest("Is this hadith authentic?"))
        assertTrue(SirajConversationPolicy.isReligiousRequest("What is the ruling on interest in Islam?"))
    }

    @Test
    fun allowsOrdinaryGeneralQuestions() {
        assertFalse(SirajConversationPolicy.isReligiousRequest("Explain gradient descent"))
        assertFalse(SirajConversationPolicy.isReligiousRequest("Help me write a Python script"))
        assertFalse(SirajConversationPolicy.isReligiousRequest("I'm building an Islamic app with Kotlin"))
    }
}
