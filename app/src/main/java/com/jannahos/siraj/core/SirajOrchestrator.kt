package com.jannahos.siraj.core

import com.jannahos.siraj.ai.SirajConversationPolicy

/**
 * Deterministic front door for SIRAJ requests.
 *
 * The orchestrator does not execute Android operations and does not invent
 * knowledge. It classifies a request into one explicit route; callers remain
 * responsible for permission checks, confirmation and execution.
 */
sealed interface SirajRoute {
    data class AppAction(
        val action: SirajAction,
        val userMessage: String
    ) : SirajRoute

    data class LocalReply(val message: String) : SirajRoute

    data class GeneralConversation(val prompt: String) : SirajRoute

    data class IslamicKnowledgeUnavailable(val prompt: String) : SirajRoute
}

object SirajOrchestrator {
    private val localHelpPhrases = setOf(
        "help", "what can you do", "what can siraj do"
    )

    fun route(
        request: String,
        surahs: List<Pair<Int, String>>
    ): SirajRoute {
        val normalized = request.trim().lowercase().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) {
            return SirajRoute.LocalReply("Tell me what you want to open or find.")
        }

        val parsed = SirajCommandParser.parse(request, surahs)
        if (parsed.action != null) {
            return SirajRoute.AppAction(parsed.action, parsed.reply)
        }

        if (SirajConversationPolicy.isReligiousRequest(request)) {
            return SirajRoute.IslamicKnowledgeUnavailable(request)
        }

        if (normalized in localHelpPhrases) {
            return SirajRoute.LocalReply(parsed.reply)
        }

        return SirajRoute.GeneralConversation(request.trim())
    }
}
