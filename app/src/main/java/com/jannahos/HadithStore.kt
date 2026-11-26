package com.jannahos

import android.content.Context
import org.json.JSONArray

object HadithStore {
    data class Hadith(val collection: String, val number: Int, val arabic: String, val english: String)

    private var cache: List<Hadith>? = null

    fun load(ctx: Context): List<Hadith> {
        cache?.let { return it }
        val list = mutableListOf<Hadith>()
        try {
            val json = ctx.assets.open("hadiths_merged.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Hadith(obj.getString("collection"), obj.getInt("number"), obj.getString("arabic"), obj.getString("english")))
            }
        } catch (e: Exception) { e.printStackTrace() }
        cache = list
        return list
    }

    suspend fun searchAsync(ctx: Context, query: String): List<Hadith> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            QuranSearchEngine.searchHadith(ctx.applicationContext, query).map { hit ->
                Hadith(hit.collection, hit.number, hit.arabic, hit.english)
            }
        }
}
