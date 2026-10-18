package com.jannahos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object MushafImageStore {
    private const val BASE_URL = "https://archive.org/download/madinah_mushaf"
    private const val TOTAL_PAGES = 604

    fun imagesDir(context: Context): File =
        File(context.filesDir, "mushaf_images").also { if (!it.exists()) it.mkdirs() }

    fun imageFile(context: Context, page: Int): File = File(imagesDir(context), "p_$page.png")

    fun isCached(context: Context, page: Int): Boolean {
        val f = imageFile(context, page)
        return f.exists() && f.length() > 1000
    }

    fun loadBitmap(context: Context, page: Int): Bitmap? {
        val f = imageFile(context, page)
        if (f.exists() && f.length() > 1000) {
            return BitmapFactory.decodeFile(f.absolutePath)
        }
        return null
    }

    fun downloadPage(context: Context, page: Int): Boolean {
        if (isCached(context, page)) return true
        return try {
            val url = "$BASE_URL/$page.png"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            if (conn.responseCode != 200) { conn.disconnect(); return false }
            val bytes = conn.inputStream.readBytes()
            conn.disconnect()
            if (bytes.size < 1000) return false
            val tmp = File(imagesDir(context), "p_$page.png.tmp")
            tmp.writeBytes(bytes)
            if (!tmp.renameTo(imageFile(context, page))) {
                imageFile(context, page).writeBytes(bytes)
                tmp.delete()
            }
            true
        } catch (e: Exception) { false }
    }

    fun downloadRange(context: Context, from: Int, to: Int, onProgress: (Int, Int) -> Unit): Boolean {
        var done = 0
        val total = to - from + 1
        for (p in from..to) {
            if (downloadPage(context, p)) done++
            onProgress(done, total)
        }
        return done == total
    }

    fun totalPages(): Int = TOTAL_PAGES
}
