package com.jannahos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object IslamicContentApi {
    suspend fun getVerse(surah: Int, ayah: Int): String? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/editions/quran-uthmani,en.sahih"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val data = JSONObject(body).getJSONArray("data")
                if (data.length() >= 2) {
                    val arabic = data.getJSONObject(0).getString("text")
                    val english = data.getJSONObject(1).getString("text")
                    return@withContext "$arabic\n\n\"$english\"\n— [Surah $surah:$ayah]"
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun getHadith(book: String, number: Int): String? = withContext(Dispatchers.IO) {
        try {
            // Clean up the book name for the API (e.g., "Sahih Muslim" -> "muslim")
            val safeBook = book.lowercase().replace("sahih", "").replace("al-", "").trim()
            val urlString = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$safeBook/$number.json"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val hadiths = root.getJSONArray("hadiths")
                if (hadiths.length() > 0) {
                    val text = hadiths.getJSONObject(0).getString("text")
                    return@withContext "${text.trim()}\n— [Sahih ${book.replaceFirstChar { it.uppercase() }}, Hadith $number]"
                }
            }
            "[Hadith not found. Check the spelling of the book (e.g., bukhari, muslim, abudawud).]"
        } catch (e: Exception) { 
            "[Hadith not found or network error.]" 
        }
    }
}
