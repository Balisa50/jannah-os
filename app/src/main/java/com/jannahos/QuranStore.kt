package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object QuranStore {

    private const val PAGE_URL = "https://api.quran.com/api/v4/verses/by_page"
    private const val TOTAL_PAGES = 604
    private const val MIN_VALID_BYTES = 200L
    private const val MAX_RETRIES = 3

    private val PARAMS =
        "mushaf=1&words=true" +
        "&word_fields=line_number,text_uthmani,position" +
        "&fields=text_uthmani,verse_key" +
        "&per_page=50"

    fun pagesDir(context: Context): File =
        File(context.filesDir, "mushaf_pages").also { if (!it.exists()) it.mkdirs() }

    fun pageFile(context: Context, page: Int): File = File(pagesDir(context), "p_$page.json")
    private fun tmpFile(context: Context, page: Int): File = File(pagesDir(context), "p_$page.json.tmp")

    fun isPageCached(context: Context, page: Int): Boolean {
        val f = pageFile(context, page)
        return f.exists() && f.length() > MIN_VALID_BYTES
    }

    fun verifyPage(context: Context, page: Int): Boolean {
        val f = pageFile(context, page)
        if (!f.exists() || f.length() <= MIN_VALID_BYTES) return false
        return try {
            val root = JSONObject(f.readText())
            val verses = root.optJSONArray("verses") ?: return false
            verses.length() > 0
        } catch (e: Exception) { false }
    }

    fun cachedCount(context: Context): Int {
        val files = pagesDir(context).listFiles() ?: return 0
        return files.count {
            it.name.startsWith("p_") && it.name.endsWith(".json") && it.length() > MIN_VALID_BYTES
        }
    }

    fun isFullyDownloaded(context: Context): Boolean = cachedCount(context) >= TOTAL_PAGES

    fun downloadPage(context: Context, page: Int): Boolean {
        if (verifyPage(context, page)) return true
        pageFile(context, page).takeIf { it.exists() }?.delete()

        repeat(MAX_RETRIES) { attempt ->
            try {
                val url = "$PAGE_URL/$page?$PARAMS"
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 20000
                conn.readTimeout = 20000
                conn.setRequestProperty("User-Agent", "JannahOS/1.0")
                conn.setRequestProperty("Accept", "application/json")
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    val root = JSONObject(body)
                    val verses = root.optJSONArray("verses")
                    if (verses != null && verses.length() > 0) {
                        writeAtomic(context, page, body)
                        return true
                    }
                } else {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                // retry
            }
            if (attempt < MAX_RETRIES - 1) Thread.sleep(400L * (attempt + 1))
        }
        return false
    }

    private fun writeAtomic(context: Context, page: Int, body: String) {
        val tmp = tmpFile(context, page)
        val dst = pageFile(context, page)
        tmp.writeText(body)
        if (!tmp.renameTo(dst)) {
            dst.writeText(body)
            tmp.delete()
        }
    }

    fun downloadRange(
        context: Context, fromPage: Int, toPage: Int, onProgress: (Int, Int) -> Unit
    ): Boolean {
        var completed = 0
        val total = toPage - fromPage + 1
        for (p in fromPage..toPage) {
            if (downloadPage(context, p)) completed++
            onProgress(completed, total)
            try { Thread.sleep(20) } catch (_: Exception) {}
        }
        return completed == total
    }

    // phase 0 = verifying, phase 1 = downloading
    fun repair(
        context: Context,
        onProgress: (phase: Int, done: Int, total: Int) -> Unit
    ): List<Int> {
        val bad = mutableListOf<Int>()
        for (p in 1..TOTAL_PAGES) {
            if (!verifyPage(context, p)) bad.add(p)
            onProgress(0, p, TOTAL_PAGES)
        }
        if (bad.isEmpty()) return emptyList()
        val failed = mutableListOf<Int>()
        var i = 0
        for (p in bad) {
            if (!downloadPage(context, p)) failed.add(p)
            i++
            onProgress(1, i, bad.size)
            try { Thread.sleep(20) } catch (_: Exception) {}
        }
        return failed
    }

    data class PageWord(val lineNumber: Int, val position: Int, val text: String, val surah: Int, val ayah: Int)

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

            var basmalaLine: Int? = null
            var headerLine: Int? = null
            val joinedLines = mutableMapOf<Int, String>()

            for ((lineNo, wordList) in wordsByLine.toSortedMap()) {
                val sorted = wordList.sortedBy { it.position }.map { it.text }
                val joined = sorted.joinToString(" ")
                joinedLines[lineNo] = joined
                if (sorted.size <= 5 && joined.contains("بِسْمِ") && joined.contains("ٱلرَّحِيمِ")) {
                    basmalaLine = lineNo
                }
                if (sorted.isNotEmpty() && wordList.any { it.ayah == 1 } && lineNo == 1) {
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
        } catch (e: Exception) {
            f.delete()
            null
        }
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
