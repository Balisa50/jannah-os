package com.jannahos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object MushafImageStore {
    private const val BASE_URL = "https://raw.githubusercontent.com/GovarJabbar/Quran-PNG/master"
    private const val TOTAL_PAGES = 604

    fun imagesDir(context: Context): File =
        File(context.filesDir, "mushaf_images").also { if (!it.exists()) it.mkdirs() }

    fun imageFile(context: Context, page: Int): File = File(imagesDir(context), "p_$page.png")

    fun loadBitmap(context: Context, page: Int): Bitmap? {
        val f = imageFile(context, page)
        if (f.exists() && f.length() > 1000) {
            return BitmapFactory.decodeFile(f.absolutePath)
        }
        return null
    }

    fun downloadAndDecode(context: Context, page: Int): Bitmap? {
        val f = imageFile(context, page)
        return try {
            // If it exists but failed to decode previously, delete it
            if (f.exists()) f.delete()

            val formattedPage = String.format("%03d", page)
            val url = URL("$BASE_URL/$formattedPage.png")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            
            if (conn.responseCode == 200) {
                val bitmap = BitmapFactory.decodeStream(conn.inputStream)
                conn.disconnect()
                
                if (bitmap != null) {
                    // Save to disk for offline use
                    f.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    return bitmap
                }
            } else {
                conn.disconnect()
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun totalPages(): Int = TOTAL_PAGES
}
