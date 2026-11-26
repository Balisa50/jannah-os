package com.jannahos.siraj.ai

/**
 * Builds a bounded, in-memory conversation window for the model.
 * Only user/assistant messages are retained; system policy is always supplied
 * separately by the caller. The window is intentionally not persisted.
 */
object SirajConversationContext {
    const val MAX_TURNS = 12
    const val MAX_MESSAGE_CHARS = 4_000
    const val MAX_TOTAL_CHARS = 16_000

    fun build(
        history: List<SirajChatMessage>,
        latestUserMessage: String
    ): List<SirajChatMessage> {
        val latest = latestUserMessage.trim().take(MAX_MESSAGE_CHARS)
        if (latest.isBlank()) return emptyList()

        val prior = history
            .asSequence()
            .filter { it.role == "user" || it.role == "assistant" }
            .filter { it.content.isNotBlank() }
            .filterNot { it.content == "Thinking…" }
            .toList()
            .takeLast(MAX_TURNS - 1)
            .map { SirajChatMessage(it.role, it.content.take(MAX_MESSAGE_CHARS)) }

        val bounded = mutableListOf<SirajChatMessage>()
        var chars = latest.length
        for (message in prior.asReversed()) {
            if (chars + message.content.length > MAX_TOTAL_CHARS) continue
            bounded.add(0, message)
            chars += message.content.length
        }
        bounded.add(SirajChatMessage("user", latest))
        return bounded
    }
}
