package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Full Quran, offline, after one download.
// The API returns { "verses": [ { "id": N, "verse_key": "surah:ayah", "text_uthmani": "..." } ] }
// We parse verse_key. Verified after download; if it does not contain
// the expected 6236 verses, we treat it as a failure and do not mark ready.
object QuranStore {

    private const val BULK_URL = "https://api.quran.com/api/v4/quran/verses/uthmani"
    private const val EXPECTED_VERSES = 6236

    fun storeFile(context: Context): File = File(context.filesDir, "quran_bulk.json")

    // Loaded lazily and cached per process.
    @Volatile private var cache: Map<Int, Map<Int, String>>? = null

    fun isDownloaded(context: Context): Boolean {
        val f = storeFile(context)
        if (!f.exists() || f.length() < 500_000) return false
        // Cheap verification: parse and count verse_keys.
        return try {
            val root = JSONObject(f.readText())
            val verses = root.optJSONArray("verses") ?: return false
            verses.length() >= EXPECTED_VERSES
        } catch (e: Exception) {
            false
        }
    }

    fun downloadAll(context: Context, onProgress: (Int) -> Unit = {}): Boolean {
        // If a stale or broken file exists, delete it first.
        val f = storeFile(context)
        if (f.exists() && !isDownloaded(context)) {
            f.delete()
        }
        if (isDownloaded(context)) return true

        return try {
            onProgress(5)
            val conn = URL(BULK_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 30000
            conn.readTimeout = 60000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) {
                conn.disconnect()
                return false
            }
            onProgress(20)
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            onProgress(80)

            val root = JSONObject(body)
            val verses = root.optJSONArray("verses") ?: return false
            if (verses.length() < EXPECTED_VERSES) return false

            f.writeText(body)
            // Reset the in-memory cache so next read re-parses fresh.
            cache = null
            onProgress(100)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun loadCache(context: Context): Map<Int, Map<Int, String>> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val result = mutableMapOf<Int, MutableMap<Int, String>>()
            try {
                val text = storeFile(context).readText()
                val root = JSONObject(text)
                val verses = root.getJSONArray("verses")
                for (i in 0 until verses.length()) {
                    val v = verses.getJSONObject(i)
                    val key = v.optString("verse_key", "")
                    val parts = key.split(":")
                    if (parts.size != 2) continue
                    val surah = parts[0].toIntOrNull() ?: continue
                    val ayah = parts[1].toIntOrNull() ?: continue
                    val arabic = v.optString("text_uthmani", "")
                    if (surah in 1..114 && ayah > 0 && arabic.isNotBlank()) {
                        result.getOrPut(surah) { mutableMapOf() }[ayah] = arabic
                    }
                }
            } catch (_: Exception) {}
            cache = result
            return result
        }
    }

    fun ayahsFor(context: Context, surah: Int): List<QuranApi.Ayah> {
        val map = loadCache(context)[surah] ?: return emptyList()
        return map.entries.sortedBy { it.key }.map { (ayahNum, arabic) ->
            QuranApi.Ayah(
                number = ayahNum,
                arabic = arabic,
                english = "",
                globalNumber = 0
            )
        }
    }

    fun surahPage(surah: Int): Int = when (surah) {
        1 -> 1; 2 -> 2; 3 -> 50; 4 -> 77; 5 -> 106; 6 -> 128; 7 -> 151
        8 -> 177; 9 -> 187; 10 -> 208; 11 -> 221; 12 -> 235; 13 -> 249
        14 -> 255; 15 -> 262; 16 -> 267; 17 -> 282; 18 -> 293; 19 -> 305
        20 -> 312; 21 -> 322; 22 -> 332; 23 -> 342; 24 -> 350; 25 -> 359
        26 -> 367; 27 -> 377; 28 -> 385; 29 -> 396; 30 -> 404; 31 -> 411
        32 -> 415; 33 -> 418; 34 -> 428; 35 -> 434; 36 -> 440; 37 -> 446
        38 -> 453; 39 -> 458; 40 -> 467; 41 -> 477; 42 -> 483; 43 -> 489
        44 -> 496; 45 -> 499; 46 -> 502; 47 -> 507; 48 -> 511; 49 -> 515
        50 -> 518; 51 -> 520; 52 -> 523; 53 -> 526; 54 -> 528; 55 -> 531
        56 -> 534; 57 -> 537; 58 -> 542; 59 -> 545; 60 -> 549; 61 -> 551
        62 -> 553; 63 -> 554; 64 -> 556; 65 -> 558; 66 -> 560; 67 -> 562
        68 -> 564; 69 -> 566; 70 -> 568; 71 -> 570; 72 -> 572; 73 -> 574
        74 -> 575; 75 -> 577; 76 -> 578; 77 -> 580; 78 -> 582; 79 -> 583
        80 -> 585; 81 -> 586; 82 -> 587; 83 -> 587; 84 -> 589; 85 -> 590
        86 -> 591; 87 -> 591; 88 -> 592; 89 -> 593; 90 -> 594; 91 -> 595
        92 -> 595; 93 -> 596; 94 -> 596; 95 -> 597; 96 -> 597; 97 -> 598
        98 -> 598; 99 -> 599; 100 -> 599; 101 -> 600; 102 -> 600; 103 -> 601
        104 -> 601; 105 -> 601; 106 -> 602; 107 -> 602; 108 -> 602
        109 -> 603; 110 -> 603; 111 -> 603; 112 -> 604; 113 -> 604; 114 -> 604
        else -> 1
    }
}
