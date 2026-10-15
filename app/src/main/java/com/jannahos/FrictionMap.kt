package com.jannahos

import android.content.Context

// Reads WakeLog and derives the user's pattern.
// No ML, no cloud. Just counts and rules we can explain.
object FrictionMap {

    data class Insight(
        val reasonCode: String,
        val label: String,
        val count: Int,
        val suggestion: String
    )

    private val SUGGESTIONS = mapOf(
        "slept_late" to "Sleep 30 minutes earlier tonight. Set a bedtime reminder.",
        "alarm_missed" to "Try a louder alarm tone, or set a backup alarm 10 minutes later.",
        "phone_far" to "Keep your phone within arm's reach tonight.",
        "went_back" to "Plan to sit up for 60 seconds before deciding to sleep again.",
        "too_tired" to "Take a nap this afternoon. Or shorten tonight's plan to two rak'ah.",
        "not_now" to "The urge to dismiss is normal. Try the wake screen once more tonight."
    )

    // Look at the last 30 days of misses and return the top recurring reason.
    fun topInsight(context: Context): Insight? {
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val db = WakeLog(context).readableDatabase
        val map = mutableMapOf<String, Int>()

        db.rawQuery(
            "SELECT reason FROM events WHERE outcome='missed' AND reason IS NOT NULL AND reason != 'not_now' AND ts > ?",
            arrayOf(cutoff.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val r = c.getString(0) ?: continue
                map[r] = (map[r] ?: 0) + 1
            }
        }

        val top = map.entries.maxByOrNull { it.value } ?: return null
        val label = when (top.key) {
            "slept_late" -> "Sleeping too late"
            "alarm_missed" -> "Alarm not waking you"
            "phone_far" -> "Phone out of reach"
            "went_back" -> "Going back to sleep"
            "too_tired" -> "Exhaustion"
            "not_now" -> "Dismissing the wake screen"
            else -> top.key
        }
        val suggestion = SUGGESTIONS[top.key] ?: "Review last week's nights."
        return Insight(top.key, label, top.value, suggestion)
    }

    // Count of distinct misses in the last 7 days, for the "you've missed N times" line.
    fun missesLast7Days(context: Context): Int {
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val db = WakeLog(context).readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM events WHERE outcome='missed' AND ts > ?",
            arrayOf(cutoff.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    // Prayers in last 7 days, for the "return" framing.
    fun prayedLast7Days(context: Context): Int {
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val db = WakeLog(context).readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM events WHERE outcome='prayed' AND ts > ?",
            arrayOf(cutoff.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }
}
