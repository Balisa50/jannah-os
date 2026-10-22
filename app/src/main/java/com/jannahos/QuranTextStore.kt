package com.jannahos

import android.content.Context
import org.json.JSONArray

object QuranTextStore {
    data class Ayah(val surah: Int, val ayah: Int, val arabic: String, val english: String)

    private var cache: List<Ayah>? = null
    private val lookupMap = mutableMapOf<String, Ayah>()

    fun load(ctx: Context): List<Ayah> {
        cache?.let { return it }
        val list = mutableListOf<Ayah>()
        try {
            val json = ctx.assets.open("quran_merged.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val ayah = Ayah(obj.getInt("surah"), obj.getInt("ayah"), obj.getString("arabic"), obj.getString("english"))
                list.add(ayah)
                lookupMap["${ayah.surah}:${ayah.ayah}"] = ayah
            }
        } catch (e: Exception) { e.printStackTrace() }
        cache = list
        return list
    }

    fun getAyah(ctx: Context, surah: Int, ayah: Int): Ayah? {
        if (cache == null) load(ctx)
        return lookupMap["$surah:$ayah"]
    }

    fun search(ctx: Context, query: String): List<Ayah> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return load(ctx).filter { it.english.contains(q, ignoreCase = true) || it.arabic.contains(q) }.take(50)
    }
}
