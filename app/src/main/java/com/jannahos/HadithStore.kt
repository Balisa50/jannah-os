package com.jannahos

import android.content.Context
import org.json.JSONArray

object HadithStore {
    data class Hadith(val text: String, val narrator: String, val source: String, val grade: String)

    suspend fun searchAsync(ctx: Context, query: String): List<Hadith> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return@withContext emptyList()
        val results = mutableListOf<Hadith>()
        
        val files = listOf("riyad_salihin.json" to "Riyad as-Salihin", "nawawi40.json" to "40 Hadith Nawawi")
        for ((file, sourceName) in files) {
            try {
                val json = ctx.assets.open(file).bufferedReader().use { it.readText() }
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    // The AhmedBaset dataset uses "english" -> "text" and "narrator" directly
                    val englishObj = obj.optJSONObject("english")
                    val english = englishObj?.optString("text") ?: ""
                    val narrator = englishObj?.optString("narrator") ?: "Unknown"
                    val grade = obj.optString("grade", "Sahih") // Grade is often at the root
                    
                    if (english.lowercase().contains(q)) {
                        results.add(Hadith(english, narrator, sourceName, grade))
                        if (results.size >= 20) return@withContext results
                    }
                }
            } catch (e: Exception) { 
                e.printStackTrace()
            }
        }
        return@withContext results
    }
}
