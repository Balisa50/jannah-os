package com.jannahos

import android.content.Context

// Tracks whether the user is currently in a prayer focus window.
object FocusSession {
    private const val PREFS = "jannah_focus"

    fun start(context: Context, prayer: String, durationMinutes: Int) {
        val until = System.currentTimeMillis() + durationMinutes * 60_000L
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong("until", until)
            .putString("prayer", prayer)
            .apply()
    }

    fun stop(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    fun isActive(context: Context): Boolean {
        val until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("until", 0L)
        return until > System.currentTimeMillis()
    }

    fun currentPrayer(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("prayer", null)

    fun remainingMs(context: Context): Long {
        val until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("until", 0L)
        return (until - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    // Whether the user has turned the feature on at all.
    fun enabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("enabled", false)

    fun setEnabled(context: Context, on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("enabled", on).apply()
    }
}
