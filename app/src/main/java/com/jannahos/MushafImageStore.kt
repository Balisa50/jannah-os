package com.jannahos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object MushafImageStore {
    private const val TOTAL_PAGES = 604

    fun loadBitmap(context: Context, page: Int): Bitmap? {
        val formatted = String.format("%03d", page)
        return try {
            // Load straight from the bundled assets
            context.assets.open("quran/$formatted.png").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            // Fallback just in case
            val f = File(context.filesDir, "p_$page.png")
            if (f.exists() && f.length() > 1000) BitmapFactory.decodeFile(f.absolutePath) else null
        }
    }

    fun totalPages(): Int = TOTAL_PAGES
}
