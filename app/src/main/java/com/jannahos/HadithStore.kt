package com.jannahos

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object HadithStore {
    data class Hadith(val collection: String, val number: Int, val arabic: String, val english: String)

    private var cache: List<Hadith>? = null

    /**
     * Compatibility list loader. It streams the compressed asset rather than
     * materialising a second full JSON string/JSONArray in memory.
     * Prefer searchAsync for normal search UI.
     */
    fun load(ctx: Context): List<Hadith> {
        cache?.let { return it }
        val list = mutableListOf<Hadith>()
        try {
            ctx.applicationContext.assets.open("hadiths_merged.json.gz").use { compressed ->
                GZIPInputStream(compressed).use { input ->
                    JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                        reader.beginArray()
                        while (reader.hasNext()) {
                            var collection = ""
                            var number = 0
                            var arabic = ""
                            var english = ""
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "collection" -> collection = reader.readStringOrEmpty()
                                    "number" -> number = reader.readStringOrEmpty().toIntOrNull() ?: 0
                                    "arabic" -> arabic = reader.readStringOrEmpty()
                                    "english" -> english = reader.readStringOrEmpty()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                            if (collection.isNotBlank() && number > 0) {
                                list += Hadith(collection, number, arabic, english)
                            }
                        }
                        reader.endArray()
                    }
                }
            }
        } catch (error: Exception) {
            android.util.Log.e("HadithStore", "Could not stream bundled Hadith data", error)
        }
        cache = list
        return list
    }

    private fun JsonReader.readStringOrEmpty(): String =
        if (peek() == JsonToken.NULL) {
            nextNull()
            ""
        } else {
            nextString()
        }

    suspend fun searchAsync(ctx: Context, query: String): List<Hadith> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            QuranSearchEngine.searchHadith(ctx.applicationContext, query).map { hit ->
                Hadith(hit.collection, hit.number, hit.arabic, hit.english)
            }
        }
}
