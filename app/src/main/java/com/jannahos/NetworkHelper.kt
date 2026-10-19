package com.jannahos

import java.net.HttpURLConnection
import java.net.URL

object NetworkHelper {
    // Mobile Circuit Breaker & Exponential Backoff
    fun fetchJson(urlString: String, maxRetries: Int = 3): String? {
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000 // 5s Timeout
                conn.readTimeout = 5000
                conn.setRequestProperty("User-Agent", "JannahOS/1.0")
                
                if (conn.responseCode == 200) {
                    return conn.inputStream.bufferedReader().use { it.readText() }
                }
                conn.disconnect()
            } catch (e: Exception) {
                // Catch timeout/network error and retry
            }
            attempt++
            if (attempt < maxRetries) {
                try { Thread.sleep((1000L * (1 shl attempt))) } catch (_: Exception) {} // Exponential backoff: 2s, 4s
            }
        }
        return null // Circuit breaker: drop to offline mode
    }
}
