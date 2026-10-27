package com.jannahos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object IslamicContentApi {
    suspend fun getVerseRange(surah: Int, startAyah: Int, endAyah: Int): String? = withContext(Dispatchers.IO) {
        try {
            if (startAyah == endAyah) {
                val urlString = "https://api.alquran.cloud/v1/ayah/" + surah + ":" + startAyah + "/editions/quran-uthmani,en.sahih"
                val conn = URL(urlString).openConnection() as HttpURLConnection
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val data = JSONObject(body).getJSONArray("data")
                    if (data.length() >= 2) {
                        val arabic = data.getJSONObject(0).getString("text")
                        val english = data.getJSONObject(1).getString("text")
                        return@withContext arabic + "\n\n\"" + english + "\"\n[Surah " + surah + ":" + startAyah + "]"
                    }
                }
                return@withContext null
            }

            val urlString = "https://api.alquran.cloud/v1/surah/" + surah + "/editions/quran-uthmani,en.sahih"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val data = JSONObject(body).getJSONArray("data")
                if (data.length() >= 2) {
                    val arabicAyahs = data.getJSONObject(0).getJSONArray("ayahs")
                    val englishAyahs = data.getJSONObject(1).getJSONArray("ayahs")
                    
                    val arabicBuilder = StringBuilder()
                    val englishBuilder = StringBuilder()
                    
                    val safeStart = (startAyah - 1).coerceAtLeast(0)
                    val safeEnd = (endAyah - 1).coerceAtMost(arabicAyahs.length() - 1)

                    for (i in safeStart..safeEnd) {
                        arabicBuilder.append(arabicAyahs.getJSONObject(i).getString("text")).append(" ")
                        englishBuilder.append(englishAyahs.getJSONObject(i).getString("text")).append(" ")
                    }
                    
                    val ref = if (startAyah == endAyah) {
                        "" + surah + ":" + startAyah
                    } else {
                        "" + surah + ":" + startAyah + "-" + endAyah
                    }
                    return@withContext arabicBuilder.toString().trim() + "\n\n\"" + englishBuilder.toString().trim() + "\"\n[Surah " + ref + "]"
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun getHadith(book: String, number: Int): String? = withContext(Dispatchers.IO) {
        try {
            val safeBook = book.lowercase().replace("sahih", "").replace("al-", "").trim()
            val urlString = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-" + safeBook + "/" + number + ".json"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val hadiths = JSONObject(body).getJSONArray("hadiths")
                if (hadiths.length() > 0) {
                    return@withContext hadiths.getJSONObject(0).getString("text").trim() + "\n[Sahih " + book.replaceFirstChar { it.uppercase() } + ", Hadith " + number + "]"
                }
            }
            "[Hadith not found. Check the spelling of the book.]"
        } catch (e: Exception) { "[Hadith not found.]" }
    }
}
