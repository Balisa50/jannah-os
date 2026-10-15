package com.jannahos

import android.content.Context
import java.util.Calendar

object TahajjudPlan {

    private const val PREFS = "jannah_tahajjud_plan"
    private const val KEY_LEVEL = "level"
    private const val KEY_NIGHTS = "nights_csv"      // e.g. "1,4" for Mon,Thu
    private const val KEY_BEDTIME_REMINDER = "bed_rem"

    // Levels: 1 night/week, 2, 3, then daily (7).
    val LEVELS = listOf(1, 2, 3, 7)

    fun level(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LEVEL, 1)

    fun setLevel(context: Context, level: Int) {
        val nights = defaultNightsFor(level)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LEVEL, level)
            .putString(KEY_NIGHTS, nights.joinToString(","))
            .apply()
    }

    fun nights(context: Context): List<Int> {
        val csv = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NIGHTS, "1") ?: "1"
        return csv.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun bedtimeReminderOn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_BEDTIME_REMINDER, true)

    fun setBedtimeReminder(context: Context, on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BEDTIME_REMINDER, on).apply()
    }

    // Is tonight an "on" night?
    fun isOnTonight(context: Context): Boolean {
        // Calendar: Sunday=1, Monday=2 ... Saturday=7 in ISO; here Calendar uses 1..7 (Sun..Sat).
        val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return nights(context).contains(dow)
    }

    private fun defaultNightsFor(level: Int): List<Int> {
        // Suggested default nights: build gradually toward Monday/Thursday (recommended sunnah days).
        return when (level) {
            1 -> listOf(Calendar.THURSDAY)          // Thursday night (Friday)
            2 -> listOf(Calendar.MONDAY, Calendar.THURSDAY)
            3 -> listOf(Calendar.MONDAY, Calendar.THURSDAY, Calendar.SATURDAY)
            else -> listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                           Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        }
    }

    // Count prayers in the last 7 days.
    fun weekProgress(context: Context): Pair<Int, Int> {
        val week = WakeLog(context).lastNights(14)
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recent = week.filter { it.ts >= cutoff }
        val prayed = recent.count { it.outcome == "prayed" }
        val target = nights(context).size
        return prayed to target
    }
}
