package com.jannahos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object HadithStore {
    data class Hadith(val id: Int, val text: String, val narrator: String, val source: String, val grade: String)

    fun search(ctx: Context, query: String): List<Hadith> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        val results = mutableListOf<Hadith>()
        
        val files = listOf("riyad_salihin.json", "nawawi40.json")
        for (file in files) {
            try {
                val json = ctx.assets.open(file).bufferedReader().use { it.readText() }
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val english = obj.optJSONObject("english")?.optString("text") ?: ""
                    val arabic = obj.optString("arabic")
                    val narrator = obj.optJSONObject("english")?.optString("narrator") ?: ""
                    val grade = obj.optJSONObject("english")?.optString("grade") ?: "Sahih"
                    
                    if (english.lowercase().contains(q) || arabic.contains(q)) {
                        results.add(Hadith(
                            id = obj.optInt("id"),
                            text = english,
                            narrator = narrator,
                            source = if (file == "nawawi40.json") "40 Hadith Nawawi" else "Riyad as-Salihin",
                            grade = grade
                        ))
                        if (results.size >= 20) return results
                    }
                }
            } catch (e: Exception) {}
        }
        return results
    }
}
