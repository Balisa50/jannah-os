package com.jannahos

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Downloads the entire Quran (by page, 604 pages) once. After that, the app
// works fully offline. Each page is stored as JSON in internal storage.
// Total download: roughly 5 MB. No tracking. No account. One HTTP call per page.
object QuranPreloader {

    private const val BASE = "https://api.quran.com/api/v4"

    fun pagesDir(context: Context): File =
        File(context.filesDir, "quran_pages").also { if (!it.exists()) it.mkdirs() }

    fun isPageCached(context: Context, page: Int): Boolean =
        File(pagesDir(context), "page_$page.json").exists()

    fun cachedCount(context: Context): Int {
        val dir = pagesDir(context)
        val files = dir.listFiles() ?: return 0
        return files.count { it.name.startsWith("page_") && it.name.endsWith(".json") }
    }

    // Download one page. Returns true on success.
    fun downloadPage(context: Context, page: Int): Boolean {
        if (isPageCached(context, page)) return true
        val url = "$BASE/verses/by_page/$page?words=true&word_fields=text_uthmani,transliteration,root_letters&fields=text_uthmani&per_page=50"
        return try {
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

            // Strip unneeded fields to keep file size small.
            val root = JSONObject(body)
            val compact = JSONObject()
            compact.put("page", page)
            compact.put("verses", root.getJSONArray("verses"))
            File(pagesDir(context), "page_$page.json").writeText(compact.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    // Load a cached page. Returns the raw JSON string or null.
    fun loadPage(context: Context, page: Int): String? {
        val f = File(pagesDir(context), "page_$page.json")
        if (!f.exists()) return null
        return try { f.readText() } catch (e: Exception) { null }
    }

    // Find the first page number of a surah (for cross-referencing).
    // Standard Madani mushaf: surah 1 is page 1, surah 2 starts page 2, etc.
    // We rely on the API to tell us where a page begins when we fetch it.
    fun totalPages(): Int = 604
}
