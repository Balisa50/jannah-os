package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// The 604 Madani mushaf pages, downloaded once and cached locally.
// Each page is stored as its own JSON file so we can render and swipe
// pages the way the printed mushaf works. After the initial download,
// the entire Quran works offline.
//
// Total size: ~6 MB. Download happens on first launch in the background.
object QuranStore {

    private const val PAGE_URL = "https://api.quran.com/api/v4/verses/by_page"
    private const val TOTAL_PAGES = 604
    private const val FIELDS = "text_uthmani,page_number"
    private const val PER_PAGE = 50

    fun pagesDir(context: Context): File =
        File(context.filesDir, "mushaf_pages").also { if (!it.exists()) it.mkdirs() }

    fun pageFile(context: Context, page: Int): File =
        File(pagesDir(context), "p_$page.json")

    fun isPageCached(context: Context, page: Int): Boolean {
        val f = pageFile(context, page)
        return f.exists() && f.length() > 200
    }

    fun cachedCount(context: Context): Int {
        val files = pagesDir(context).listFiles() ?: return 0
        return files.count { it.name.startsWith("p_") && it.name.endsWith(".json") }
    }

    fun isFullyDownloaded(context: Context): Boolean =
        cachedCount(context) >= TOTAL_PAGES

    // Download a single page. Returns true on success.
    fun downloadPage(context: Context, page: Int): Boolean {
        if (isPageCached(context, page)) return true
        return try {
            val url = "$PAGE_URL/$page?fields=$FIELDS&per_page=$PER_PAGE"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 20000
            conn.readTimeout = 20000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) {
                conn.disconnect()
                return false
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            // Validate JSON has verses before saving
            val root = JSONObject(body)
            val verses = root.optJSONArray("verses") ?: return false
            if (verses.length() == 0) return false
            pageFile(context, page).writeText(body)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Download pages in a range, calling onProgress with completed count.
    // Blocks until done or failure. Run on a background thread.
    fun downloadRange(
        context: Context,
        fromPage: Int,
        toPage: Int,
        onProgress: (Int, Int) -> Unit
    ): Boolean {
        var completed = 0
        val total = toPage - fromPage + 1
        for (p in fromPage..toPage) {
            if (downloadPage(context, p)) {
                completed++
                onProgress(completed, total)
            }
            // gentle delay to avoid hammering the API
            try { Thread.sleep(40) } catch (_: Exception) {}
        }
        return completed == total
    }

    // Load a page's verse list. Returns null if not cached.
    data class PageVerse(val surah: Int, val ayah: Int, val arabic: String)

    fun loadPage(context: Context, page: Int): List<PageVerse>? {
        val f = pageFile(context, page)
        if (!f.exists()) return null
        return try {
            val root = JSONObject(f.readText())
            val arr = root.getJSONArray("verses")
            val out = mutableListOf<PageVerse>()
            for (i in 0 until arr.length()) {
                val v = arr.getJSONObject(i)
                val key = v.optString("verse_key", "")
                val parts = key.split(":")
                if (parts.size != 2) continue
                val surah = parts[0].toIntOrNull() ?: continue
                val ayah = parts[1].toIntOrNull() ?: continue
                val text = v.optString("text_uthmani", "")
                if (text.isNotBlank()) out.add(PageVerse(surah, ayah, text))
            }
            out
        } catch (e: Exception) { null }
    }

    // Given a surah number, return the first page it appears on.
    // Standard Madani mushaf anchors. Used for jumping to a surah.
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
