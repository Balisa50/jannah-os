package com.jannahos.siraj.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajProviderEndpointPolicyTest {
    @Test
    fun acceptsHttpsProviderEndpoints() {
        assertTrue(SirajProviderEndpointPolicy.isAllowed("https://openrouter.ai/api/v1"))
        assertTrue(SirajProviderEndpointPolicy.isAllowed("https://api.example.com"))
    }

    @Test
    fun rejectsHttpAndNonHttpSchemes() {
        assertFalse(SirajProviderEndpointPolicy.isAllowed("http://localhost:8080"))
        assertFalse(SirajProviderEndpointPolicy.isAllowed("file:///tmp/key"))
        assertFalse(SirajProviderEndpointPolicy.isAllowed("javascript:alert(1)"))
    }

    @Test
    fun rejectsCredentialsQueriesAndFragmentsInEndpoint() {
        assertFalse(SirajProviderEndpointPolicy.isAllowed("https://user:pass@example.com/v1"))
        assertFalse(SirajProviderEndpointPolicy.isAllowed("https://example.com/v1?token=secret"))
        assertFalse(SirajProviderEndpointPolicy.isAllowed("https://example.com/v1#fragment"))
    }
}
