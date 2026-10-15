package com.jannahos

import android.content.Context

object QuranBookmarks {
    private const val PREFS = "jannah_quran_marks"

    fun setLastSurah(context: Context, surah: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("last_surah", surah).apply()
    }

    fun lastSurah(context: Context): Int? {
        val v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("last_surah", -1)
        return if (v > 0) v else null
    }
}
