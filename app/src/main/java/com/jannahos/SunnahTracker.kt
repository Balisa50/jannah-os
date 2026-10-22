package com.jannahos

import android.content.Context

// Tracks the recommended sunnah prayers around the five daily prayers.
// Resets daily. No streaks, no shaming.
object SunnahTracker {

    private const val PREFS = "jannah_sunnah"

    data class Sunnah(val id: String, val label: String, val rakah: Int, val note: String)

    val rawatib = listOf(
        Sunnah("fajr_before", "Fajr · 2 before", 2, "The Prophet  never left these two."),
        Sunnah("dhuhr_before", "Dhuhr · 4 before", 4, ""),
        Sunnah("dhuhr_after", "Dhuhr · 2 after", 2, ""),
        Sunnah("maghrib_after", "Maghrib · 2 after", 2, ""),
        Sunnah("isha_after", "Isha · 2 after", 2, ""),
        Sunnah("witr", "Witr · odd night prayer", 1, "The Prophet  said: \"Allah is Witr and He loves witr.\""),
        Sunnah("duha", "Duha · forenoon", 2, "Recommended between sunrise and Dhuhr.")
    )

    private fun key(id: String): String = "${id}_${WakeLog.todayStamp()}"

    fun isDone(context: Context, id: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(key(id), false)

    fun toggle(context: Context, id: String): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = !p.getBoolean(key(id), false)
        p.edit().putBoolean(key(id), now).apply()
        return now
    }

    fun doneToday(context: Context): Int = rawatib.count { isDone(context, it.id) }
}
