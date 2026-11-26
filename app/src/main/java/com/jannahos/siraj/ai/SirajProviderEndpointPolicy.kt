package com.jannahos.siraj.ai

/** Strict endpoint validation before an API credential is attached to a request. */
internal object SirajProviderEndpointPolicy {
    fun isAllowed(value: String): Boolean = runCatching {
        val uri = java.net.URI(value)
        val host = uri.host ?: return@runCatching false
        uri.scheme.equals("https", ignoreCase = true) &&
            host.isNotBlank() &&
            uri.userInfo == null &&
            uri.query == null &&
            uri.fragment == null
    }.getOrDefault(false)
}
