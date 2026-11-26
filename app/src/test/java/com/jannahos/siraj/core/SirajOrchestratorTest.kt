package com.jannahos.siraj.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajOrchestratorTest {
    private val surahs = listOf(1 to "Al-Fatihah", 2 to "Al-Baqarah", 18 to "Al-Kahf")

    @Test
    fun routesKnownAppCommandsToAllowlistedActions() {
        val route = SirajOrchestrator.route("Open Surah Al-Kahf", surahs)
        assertTrue(route is SirajRoute.AppAction)
        assertEquals(SirajAction.OpenSurah(18, "Al-Kahf"), (route as SirajRoute.AppAction).action)
    }

    @Test
    fun blocksReligiousQuestionsFromGeneralModel() {
        assertTrue(SirajOrchestrator.route("What does this hadith mean?", surahs) is SirajRoute.IslamicKnowledgeUnavailable)
    }

    @Test
    fun sendsOrdinaryQuestionsToGeneralConversation() {
        assertTrue(SirajOrchestrator.route("Explain Bayesian inference simply", surahs) is SirajRoute.GeneralConversation)
    }

    @Test
    fun handlesHelpLocally() {
        assertTrue(SirajOrchestrator.route("help", surahs) is SirajRoute.LocalReply)
    }

    @Test
    fun rejectsBlankRequestsWithoutCallingModel() {
        assertTrue(SirajOrchestrator.route("   ", surahs) is SirajRoute.LocalReply)
    }
}
