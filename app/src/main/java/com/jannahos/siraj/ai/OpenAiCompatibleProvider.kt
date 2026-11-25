package com.jannahos.siraj.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SirajChatMessage(val role: String, val content: String)

sealed interface SirajInferenceResult {
    data class Success(val text: String) : SirajInferenceResult
    data class Failure(val message: String) : SirajInferenceResult
}

/** Optional OpenAI-compatible transport. Offline features do not depend on it. */
class OpenAiCompatibleProvider(private val context: Context) {
    suspend fun complete(
        messages: List<SirajChatMessage>,
        maxTokens: Int = 700
    ): SirajInferenceResult = withContext(Dispatchers.IO) {
        val config = SirajProviderSettings.load(context.applicationContext)
            ?: return@withContext SirajInferenceResult.Failure(
                "No AI provider is configured. SIRAJ's offline features remain available."
            )
        val apiKey = config.apiKey
        if (apiKey.isNullOrBlank()) {
            return@withContext SirajInferenceResult.Failure(
                "The configured AI provider has no credential. Add one in AI settings."
            )
        }
        if (messages.isEmpty() || messages.size > 24 ||
            messages.any {
                it.content.isBlank() || it.content.length > 12_000 ||
                    it.role !in setOf("system", "user", "assistant")
            }
        ) {
            return@withContext SirajInferenceResult.Failure(
                "This conversation is too large or contains an invalid message."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val base = config.endpoint.trimEnd('/')
            val requestUrl = if (base.endsWith("/v1")) {
                "$base/chat/completions"
            } else {
                "$base/v1/chat/completions"
            }
            connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val payload = JSONObject()
                .put("model", config.model)
                .put("messages", JSONArray().apply {
                    messages.forEach { message ->
                        put(JSONObject().put("role", message.role).put("content", message.content))
                    }
                })
                .put("temperature", 0.4)
                .put("max_tokens", maxTokens.coerceIn(64, 1200))

            connection.outputStream.use {
                it.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val responseStream = if (status in 200..299) connection.inputStream
            else connection.errorStream
            val body = responseStream?.bufferedReader()?.use { readBounded(it, 1_000_000) }.orEmpty()
            if (body.length >= 1_000_000) {
                return@withContext SirajInferenceResult.Failure("The AI provider response was too large.")
            }

            if (status !in 200..299) {
                return@withContext SirajInferenceResult.Failure(
                    when (status) {
                        401, 403 -> "The AI provider rejected the credential. Check the key and provider access."
                        404 -> "The AI model or chat-completions endpoint was not found."
                        429 -> "The AI provider is rate-limiting requests. Try again later."
                        in 500..599 -> "The AI provider is temporarily unavailable."
                        else -> "The AI provider returned HTTP " + status + "."
                    }
                )
            }

            val responseText = JSONObject(body)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
                .orEmpty()

            if (responseText.isBlank()) SirajInferenceResult.Failure(
                "The AI provider returned an empty response."
            ) else SirajInferenceResult.Success(responseText.take(12_000))
        } catch (_: java.net.SocketTimeoutException) {
            SirajInferenceResult.Failure("The AI provider took too long to respond.")
        } catch (_: java.net.UnknownHostException) {
            SirajInferenceResult.Failure("The AI provider could not be reached. Check your connection.")
        } catch (failure: Exception) {
            // Never log request bodies, headers, provider credentials or raw errors.
            Log.w("SIRAJ_AI", "Provider request failed (" + failure.javaClass.simpleName + ").")
            SirajInferenceResult.Failure("The AI request failed. Check the provider settings and try again.")
        } finally {
            connection?.disconnect()
        }
    }
    private fun readBounded(reader: java.io.Reader, maxChars: Int): String {
        val output = StringBuilder()
        val buffer = CharArray(8192)
        while (output.length <= maxChars) {
            val count = reader.read(buffer, 0, minOf(buffer.size, maxChars + 1 - output.length))
            if (count < 0) break
            output.append(buffer, 0, count)
        }
        return output.toString()
    }

}
