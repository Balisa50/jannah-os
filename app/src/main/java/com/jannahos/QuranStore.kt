package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object QuranStore {

    private const val PAGE_URL = "https://api.quran.com/api/v4/verses/by_page"
    private const val TOTAL_PAGES = 604

    private val PARAMS =
        "mushaf=1&words=true" +
        "&word_fields=line_number,text_uthmani,position" +
        "&fields=text_uthmani,verse_key" +
        "&per_page=50"

    fun pagesDir(context: Context): File =
        File(context.filesDir, "mushaf_pages").also { if (!it.exists()) it.mkdirs() }

    fun pageFile(context: Context, page: Int): File = File(pagesDir(context), "p_$page.json")

    fun isPageCached(context: Context, page: Int): Boolean {
        val f = pageFile(context, page)
        return f.exists() && f.length() > 200
    }

    fun cachedCount(context: Context): Int {
        val files = pagesDir(context).listFiles() ?: return 0
        return files.count { it.name.startsWith("p_") && it.name.endsWith(".json") }
    }

    fun isFullyDownloaded(context: Context): Boolean = cachedCount(context) >= TOTAL_PAGES

    fun downloadPage(context: Context, page: Int): Boolean {
        if (isPageCached(context, page)) return true
        return try {
            val url = "$PAGE_URL/$page?$PARAMS"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 20000
            conn.readTimeout = 20000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) { conn.disconnect(); return false }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val verses = root.optJSONArray("verses") ?: return false
            if (verses.length() == 0) return false
            pageFile(context, page).writeText(body)
            true
        } catch (e: Exception) { false }
    }

    fun downloadRange(
        context: Context, fromPage: Int, toPage: Int, onProgress: (Int, Int) -> Unit
    ): Boolean {
        var completed = 0
        val total = toPage - fromPage + 1
        for (p in fromPage..toPage) {
            if (downloadPage(context, p)) { completed++; onProgress(completed, total) }
            try { Thread.sleep(40) } catch (_: Exception) {}
        }
        return completed == total
    }

    // A single word with its exact line position on the page
    data class PageWord(val lineNumber: Int, val position: Int, val text: String, val surah: Int, val ayah: Int)

    // Load a page and reconstruct the exact 15-line mushaf layout.
    // Returns a map of lineNumber -> joined text string.
    // Also returns the surah number at the start of the page.
    data class PageLayout(
        val lines: Map<Int, String>,
        val firstSurah: Int,
        val basmalaLineNumber: Int?,
        val surahHeaderLineNumber: Int?
    )

    fun loadPage(context: Context, page: Int): PageLayout? {
        val f = pageFile(context, page)
        if (!f.exists()) return null
        return try {
            val root = JSONObject(f.readText())
            val verses = root.getJSONArray("verses")

            val wordsByLine = mutableMapOf<Int, MutableList<PageWord>>()
            var firstSurah = 1
            var hasBasmala = false
            var hasSurahStart = false

            for (v in 0 until verses.length()) {
                val verse = verses.getJSONObject(v)
                val key = verse.optString("verse_key", "")
                val parts = key.split(":")
                if (parts.size != 2) continue
                val surah = parts[0].toIntOrNull() ?: continue
                val ayah = parts[1].toIntOrNull() ?: continue
                if (v == 0) firstSurah = surah

                val wordsArr = verse.optJSONArray("words") ?: continue
                for (w in 0 until wordsArr.length()) {
                    val word = wordsArr.getJSONObject(w)
                    val lineNo = word.optInt("line_number", 0)
                    val text = word.optString("text_uthmani", "").trim()
                    val pos = word.optInt("position", w + 1)
                    if (lineNo > 0 && text.isNotBlank()) {
                        wordsByLine.getOrPut(lineNo) { mutableListOf() }
                            .add(PageWord(lineNo, pos, text, surah, ayah))
                    }
                }
            }

            // Find the basmala line (short, contains "بِسْمِ") and surah start line
            var basmalaLine: Int? = null
            var headerLine: Int? = null

            val joinedLines = mutableMapOf<Int, String>()
            for ((lineNo, wordList) in wordsByLine.toSortedMap()) {
                val sorted = wordList.sortedBy { it.position }.map { it.text }
                val joined = sorted.joinToString(" ")
                joinedLines[lineNo] = joined

                // Basmala detection: line with only basmala words
                if (sorted.size <= 5 && joined.contains("بِسْمِ") && joined.contains("ٱلرَّحِيمِ")) {
                    basmalaLine = lineNo
                }
                // Surah start detection: first word is ayah 1
                if (sorted.isNotEmpty() && wordList.any { it.ayah == 1 } && lineNo == 1) {
                    // crude but works for pages that start a new surah
                    hasSurahStart = true
                    headerLine = lineNo
                }
            }

            PageLayout(
                lines = joinedLines,
                firstSurah = firstSurah,
                basmalaLineNumber = basmalaLine,
                surahHeaderLineNumber = if (hasSurahStart) headerLine else null
            )
        } catch (e: Exception) { null }
    }

    fun firstPageOfSurah(surah: Int): Int = when (surah) {
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

    fun totalPages(): Int = TOTAL_PAGES
}
