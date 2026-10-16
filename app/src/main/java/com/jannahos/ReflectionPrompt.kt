package com.jannahos

import android.content.Context

// Tracks whether the user has already seen the morning reflection today.
object ReflectionPrompt {
    private const val PREFS = "jannah_reflection_prompt"

    fun alreadySeenToday(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getString("day", "") == WakeLog.todayStamp()
    }

    fun markSeenToday(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("day", WakeLog.todayStamp()).apply()
    }

    // Returns true if yesterday's event log contains a successful prayer.
    fun prayedLastNight(context: Context): Boolean {
        val cutoff = System.currentTimeMillis() - 20L * 60 * 60 * 1000
        val events = WakeLog(context).lastNights(5)
        return events.any { it.outcome == "prayed" && it.ts >= cutoff }
    }

    fun shouldPrompt(context: Context): Boolean {
        if (alreadySeenToday(context)) return false
        if (!prayedLastNight(context)) return false
        // Only show in the morning window, not all day.
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 5..11
    }
}
