package com.jannahos.siraj.memory

/** Deterministic local commands; memory requests never fall through to an AI provider. */
object SirajMemoryCommandParser {
    fun rememberContent(request: String): String? {
        val trimmed = request.trim()
        val content = when {
            trimmed.startsWith("remember:", ignoreCase = true) ->
                trimmed.substringAfter(':').trim()
            trimmed.startsWith("remember that ", ignoreCase = true) ->
                trimmed.substringAfter(' ').substringAfter(' ').trim()
            else -> return null
        }
        return content.takeIf { it.isNotBlank() }?.take(500)
    }

    fun forgetQuery(request: String): String? {
        val trimmed = request.trim()
        if (!trimmed.startsWith("forget:", ignoreCase = true)) return null
        return trimmed.substringAfter(':').trim().takeIf { it.isNotBlank() }?.take(200)
    }

    fun isShowMemoryRequest(request: String): Boolean =
        request.trim().lowercase() in setOf(
            "show memory", "my memory", "what do you remember", "show my memory"
        )

    fun isClearMemoryRequest(request: String): Boolean =
        request.trim().lowercase() in setOf("clear memory", "forget all memory")
}
