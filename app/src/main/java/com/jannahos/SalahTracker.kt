package com.jannahos

import android.content.Context

object SalahTracker {
    private const val PREFS = "jannah_salah"
    private val PRAYERS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    fun today(context: Context): Map<String, Boolean> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val stored = p.getString("day", "")
        if (stored != today) {
            p.edit().putString("day", today)
                .putBoolean("fajr", false)
                .putBoolean("dhuhr", false)
                .putBoolean("asr", false)
                .putBoolean("maghrib", false)
                .putBoolean("isha", false)
                .apply()
        }
        return PRAYERS.associateWith { p.getBoolean(it, false) }
    }

    fun toggle(context: Context, prayer: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val stored = p.getString("day", "")
        if (stored != today) {
            p.edit().putString("day", today).apply()
        }
        val current = p.getBoolean(prayer, false)
        p.edit().putBoolean(prayer, !current).apply()
    }

    fun label(prayer: String): String = when (prayer) {
        "fajr" -> "Fajr"
        "dhuhr" -> "Dhuhr"
        "asr" -> "Asr"
        "maghrib" -> "Maghrib"
        "isha" -> "Isha"
        else -> prayer
    }
}
