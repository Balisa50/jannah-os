package com.jannahos

import android.content.Context
import org.json.JSONArray

object QuranTextStore {
    data class Ayah(val surah: Int, val ayah: Int, val text: String)

    private var cache: List<Ayah>? = null
    private val lookupMap = mutableMapOf<String, Ayah>()

    fun load(ctx: Context): List<Ayah> {
        cache?.let { return it }
        val list = mutableListOf<Ayah>()
        try {
            val json = ctx.assets.open("quran_uthmani.json").bufferedReader().use { it.readText() }
            val root = org.json.JSONObject(json)
            val surahs = root.getJSONArray("surahs")
            for (i in 0 until surahs.length()) {
                val s = surahs.getJSONObject(i)
                val surahNum = s.getInt("number")
                val ayahs = s.getJSONArray("ayahs")
                for (j in 0 until ayahs.length()) {
                    val a = ayahs.getJSONObject(j)
                    list.add(Ayah(surahNum, a.getInt("number"), a.getString("text")))
                }
            }
        } catch (e: Exception) {}
        cache = list
        list.forEach { lookupMap["${it.surah}:${it.ayah}"] = it }
        return list
    }

    suspend fun searchAsync(ctx: Context, query: String): List<Ayah> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        search(ctx, query)
    }

    fun getAyah(ctx: Context, surah: Int, ayah: Int): Ayah? {
        if (lookupMap.isEmpty()) load(ctx)
        return lookupMap["$surah:$ayah"]
    }

    fun search(ctx: Context, query: String): List<Ayah> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return load(ctx).filter { it.text.contains(q) }.take(50)
    }
}
