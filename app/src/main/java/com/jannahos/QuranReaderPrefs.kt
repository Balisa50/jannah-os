package com.jannahos

import android.content.Context

object QuranReaderPrefs {
    private const val PREFS = "jannah_quran_reader"

    fun fontSize(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("font_size", 28)

    fun setFontSize(context: Context, sp: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("font_size", sp.coerceIn(18, 48)).apply()
    }

    fun showTranslation(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("show_translation", false)

    fun setShowTranslation(context: Context, on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("show_translation", on).apply()
    }

    fun lastSurah(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("last_surah", 0)

    fun setLastSurah(context: Context, surah: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("last_surah", surah).apply()
    }

    fun lastAyah(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("last_ayah", 0)

    fun setLastAyah(context: Context, ayah: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("last_ayah", ayah).apply()
    }
}
