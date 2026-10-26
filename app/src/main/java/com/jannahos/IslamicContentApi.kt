package com.jannahos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object IslamicContentApi {
    // Uses alquran.cloud for both Arabic and English
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
            // Standardizes book names
            val safeBook = book.lowercase().replace("sahih", "").trim()
            val urlString = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$safeBook/$number.json"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val hadiths = JSONObject(body).getJSONArray("hadiths")
                if (hadiths.length() > 0) {
                    return@withContext "${hadiths.getJSONObject(0).getString("text")}\n— [Sahih $book, Hadith $number]"
                }
            }
            null
        } catch (e: Exception) { null }
    }
}
