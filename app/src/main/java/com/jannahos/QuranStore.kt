package com.jannahos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// The entire Quran, offline, after one download.
// Uses quran.com's single-call endpoint that returns all 6236 verses
// as Uthmani text. Downloaded once. Then everything works offline.
object QuranStore {

    private const val BULK_URL = "https://api.quran.com/api/v4/quran/verses/uthmani"

    fun storeFile(context: Context): File =
        File(context.filesDir, "quran_bulk.json")

    fun isDownloaded(context: Context): Boolean =
        storeFile(context).exists() && storeFile(context).length() > 100_000

    // Download the entire Quran. Returns true on success.
    // ~1.5 MB transfer, no chunking needed.
    fun downloadAll(context: Context, onProgress: (Int) -> Unit = {}): Boolean {
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
            // Validate it parses before saving
            val root = JSONObject(body)
            root.getJSONArray("verses")
            storeFile(context).writeText(body)
            onProgress(100)
            true
        } catch (e: Exception) {
            false
        }
    }

    // In-memory cache of parsed verses, indexed by surah then ayah.
    // Loaded once on first access per process.
    @Volatile private var cache: Map<Int, Map<Int, String>>? = null

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
                    val surah = v.optInt("surah_number", 0)
                    val ayah = v.optInt("verse_number", 0)
                    val ar = v.optString("text_uthmani", "")
                    if (surah in 1..114 && ayah > 0) {
                        result.getOrPut(surah) { mutableMapOf() }[ayah] = ar
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

    // Map ayah range to a Madani mushaf page. We approximate because the
    // canonical pagination is fixed by tradition. A more accurate mapping
    // would use a static 604-entry table; this is a good default.
    private val PAGE_STARTS: List<Pair<Int, Int>> = listOf(
        // (startSurah, startAyah) for each of 604 pages. Abridged here for
        // core surahs; falls back to surah 1 page 1 otherwise.
        1 to 1, 2 to 1, 2 to 6, 2 to 17, 2 to 25, 2 to 30, 2 to 38,
        2 to 49, 2 to 58, 2 to 62, 2 to 70, 2 to 77, 2 to 84, 2 to 89,
        2 to 94, 2 to 102, 2 to 106, 2 to 113, 2 to 120, 2 to 127,
        2 to 135, 2 to 142, 2 to 146, 2 to 154, 2 to 164, 2 to 170,
        2 to 177, 2 to 182, 2 to 187, 2 to 191, 2 to 197, 2 to 203,
        2 to 211, 2 to 216, 2 to 220, 2 to 225, 2 to 231, 2 to 235,
        2 to 238, 2 to 246, 2 to 249, 2 to 253, 2 to 257, 2 to 260,
        2 to 265, 2 to 270, 2 to 275, 2 to 282, 2 to 283, 3 to 1
        // ... this would continue for all 604 pages. For now the reader
        // falls back to surah-level navigation.
    )

    fun surahPage(surah: Int): Int {
        // Simple heuristic: known surah-to-page anchors. Improved later.
        return when (surah) {
            1 -> 1
            2 -> 2
            3 -> 50
            4 -> 77
            5 -> 106
            6 -> 128
            7 -> 151
            8 -> 177
            9 -> 187
            10 -> 208
            11 -> 221
            12 -> 235
            13 -> 249
            14 -> 255
            15 -> 262
            16 -> 267
            17 -> 282
            18 -> 293
            19 -> 305
            20 -> 312
            21 -> 322
            22 -> 332
            23 -> 342
            24 -> 350
            25 -> 359
            26 -> 367
            27 -> 377
            28 -> 385
            29 -> 396
            30 -> 404
            31 -> 411
            32 -> 415
            33 -> 418
            34 -> 428
            35 -> 434
            36 -> 440
            37 -> 446
            38 -> 453
            39 -> 458
            40 -> 467
            41 -> 477
            42 -> 483
            43 -> 489
            44 -> 496
            45 -> 499
            46 -> 502
            47 -> 507
            48 -> 511
            49 -> 515
            50 -> 518
            51 -> 520
            52 -> 523
            53 -> 526
            54 -> 528
            55 -> 531
            56 -> 534
            57 -> 537
            58 -> 542
            59 -> 545
            60 -> 549
            61 -> 551
            62 -> 553
            63 -> 554
            64 -> 556
            65 -> 558
            66 -> 560
            67 -> 562
            68 -> 564
            69 -> 566
            70 -> 568
            71 -> 570
            72 -> 572
            73 -> 574
            74 -> 575
            75 -> 577
            76 -> 578
            77 -> 580
            78 -> 582
            79 -> 583
            80 -> 585
            81 -> 586
            82 -> 587
            83 -> 587
            84 -> 589
            85 -> 590
            86 -> 591
            87 -> 591
            88 -> 592
            89 -> 593
            90 -> 594
            91 -> 595
            92 -> 595
            93 -> 596
            94 -> 596
            95 -> 597
            96 -> 597
            97 -> 598
            98 -> 598
            99 -> 599
            100 -> 599
            101 -> 600
            102 -> 600
            103 -> 601
            104 -> 601
            105 -> 601
            106 -> 602
            107 -> 602
            108 -> 602
            109 -> 603
            110 -> 603
            111 -> 603
            112 -> 604
            113 -> 604
            114 -> 604
            else -> 1
        }
    }
}
