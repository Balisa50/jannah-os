package com.jannahos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object IslamicContentApi {
    suspend fun getVerse(surah: Int, ayah: Int): String? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.quran.com/api/v4/quran/verses/uthmani?chapter_number=$surah&verse_number=$ayah"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val verses = JSONObject(body).getJSONArray("verses")
                if (verses.length() > 0) {
                    return@withContext "${verses.getJSONObject(0).getString("text_uthmani")}\n\n[Surah $surah:$ayah]"
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun getHadith(book: String, number: Int): String? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$book/1.json"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val hadiths = JSONObject(body).getJSONArray("hadiths")
                if (hadiths.length() > 0) {
                    return@withContext hadiths.getJSONObject(0).getString("text")
                }
            }
            null
        } catch (e: Exception) { null }
    }
}
