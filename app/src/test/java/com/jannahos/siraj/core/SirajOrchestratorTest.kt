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
        val route = SirajOrchestrator.route("What does this hadith mean?", surahs)
        assertTrue(route is SirajRoute.IslamicKnowledgeUnavailable)
    }

    @Test
    fun sendsOrdinaryQuestionsToGeneralConversation() {
        val route = SirajOrchestrator.route("Explain Bayesian inference simply", surahs)
        assertTrue(route is SirajRoute.GeneralConversation)
    }

    @Test
    fun handlesHelpLocally() {
        val route = SirajOrchestrator.route("help", surahs)
        assertTrue(route is SirajRoute.LocalReply)
    }

    @Test
    fun rejectsBlankRequestsWithoutCallingModel() {
        val route = SirajOrchestrator.route("   ", surahs)
        assertTrue(route is SirajRoute.LocalReply)
    }
}
