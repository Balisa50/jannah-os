package com.jannahos

import android.content.Context

// Simple per-day tracker for character traits.
// One tap per trait, resets each day, no streaks, no punishment.
object CharacterLog {

    private const val PREFS = "jannah_character"

    data class Trait(val id: String, val label: String, val hint: String)

    val traits = listOf(
        Trait("patience", "Patience", "Did you hold back from reacting in anger today?"),
        Trait("honesty", "Honesty", "Did you speak truthfully, even when it was hard?"),
        Trait("gratitude", "Gratitude", "Did you pause and thank Allah for something specific?"),
        Trait("kindness", "Kindness", "Did you do one small kindness without being seen?"),
        Trait("restraint", "Restraint", "Did you lower your gaze / your tongue where you should have?"),
        Trait("connection", "Connection", "Did you make dua or read Quran outside of the obligatory?"),
        Trait("family", "Family", "Did you honour a parent, sibling, spouse, or child today?")
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

    fun doneToday(context: Context): Int =
        traits.count { isDone(context, it.id) }

    // Clean old entries beyond 30 days.
    fun prune(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = p.edit()
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        p.all.keys.forEach { k ->
            val parts = k.split("_")
            if (parts.size >= 2) {
                val datePart = parts.last()
                try {
                    val d = fmt.parse(datePart)
                    if (d != null && d.time < cutoff) editor.remove(k)
                } catch (_: Exception) {}
            }
        }
        editor.apply()
    }
}
