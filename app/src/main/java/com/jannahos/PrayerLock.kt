package com.jannahos

import android.content.Context

// When enabled, the app locks the phone to the prayer commitment window
// for a user-chosen duration (default 10 minutes) each time a prayer
// alarm fires. The lock is enforced by the accessibility service, which
// returns the user to the prayer screen if they open any other app.
//
// Cannot be unblocked by the user except by finishing the prayer or
// waiting out the timer. Fully opt-in. Fully customizable in Settings.
object PrayerLock {
    private const val PREFS = "jannah_prayer_lock"

    fun enabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("enabled", false)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("enabled", on).apply()
    }

    fun durationMinutes(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("duration", 10)

    fun setDurationMinutes(ctx: Context, minutes: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("duration", minutes.coerceIn(5, 30)).apply()
    }

    // Session state
    private const val SESSION = "jannah_prayer_lock_session"

    fun startSession(ctx: Context, prayer: String) {
        val until = System.currentTimeMillis() + durationMinutes(ctx) * 60_000L
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .edit()
            .putLong("until", until)
            .putString("prayer", prayer)
            .apply()
    }

    fun stopSession(ctx: Context) {
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    fun sessionActive(ctx: Context): Boolean {
        val until = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .getLong("until", 0L)
        return until > System.currentTimeMillis()
    }

    fun sessionPrayer(ctx: Context): String? =
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .getString("prayer", null)

    fun sessionRemainingMs(ctx: Context): Long {
        val until = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .getLong("until", 0L)
        return (until - System.currentTimeMillis()).coerceAtLeast(0L)
    }
}
