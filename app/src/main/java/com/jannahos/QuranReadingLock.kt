package com.jannahos

import android.content.Context

// Quran reading lock. If enabled, once per day the app takes over the
// phone until the user has scrolled through at least one page of Quran
// (roughly 15 lines of Arabic). A 7-minute countdown runs the whole time.
// Leaving the screen resets the timer to 7:00 — the goal is reading, not
// staring at the screen for seven minutes.
object QuranReadingLock {

    private const val PREFS = "jannah_quran_lock"
    private const val SESSION = "jannah_quran_lock_session"

    private const val KEY_ENABLED = "enabled"
    private const val KEY_DONE_DAY = "done_day"

    // Session
    private const val KEY_STARTED_AT = "started_at"
    private const val KEY_TARGET_MS = "target_ms"
    private const val KEY_LINES_READ = "lines_read"

    const val DEFAULT_MINUTES = 7
    const val LINES_PER_PAGE = 15

    fun enabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, on).apply()
    }

    fun isDoneToday(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DONE_DAY, "") == WakeLog.todayStamp()

    fun markDoneToday(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_DONE_DAY, WakeLog.todayStamp()).apply()
    }

    // Whether the app should currently be locking the phone for Quran.
    fun shouldLock(ctx: Context): Boolean {
        if (!enabled(ctx)) return false
        if (isDoneToday(ctx)) return false
        return true
    }

    // Session management — starts or resumes today's lock session.
    fun startOrResume(ctx: Context) {
        val p = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
        if (p.contains(KEY_STARTED_AT)) return
        p.edit()
            .putLong(KEY_STARTED_AT, System.currentTimeMillis())
            .putLong(KEY_TARGET_MS, DEFAULT_MINUTES * 60_000L)
            .putInt(KEY_LINES_READ, 0)
            .apply()
    }

    // Called when user leaves the Quran screen. Resets the timer.
    fun resetTimer(ctx: Context) {
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_STARTED_AT, System.currentTimeMillis())
            .apply()
    }

    fun incrementLines(ctx: Context, delta: Int) {
        val p = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
        val current = p.getInt(KEY_LINES_READ, 0)
        p.edit().putInt(KEY_LINES_READ, current + delta).apply()
    }

    fun remainingMs(ctx: Context): Long {
        val p = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
        val target = p.getLong(KEY_TARGET_MS, DEFAULT_MINUTES * 60_000L)
        val started = p.getLong(KEY_STARTED_AT, System.currentTimeMillis())
        return (started + target - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun linesRead(ctx: Context): Int =
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .getInt(KEY_LINES_READ, 0)

    fun isSatisfied(ctx: Context): Boolean {
        val p = ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
        val target = p.getLong(KEY_TARGET_MS, DEFAULT_MINUTES * 60_000L)
        val started = p.getLong(KEY_STARTED_AT, System.currentTimeMillis())
        val timeUp = started + target <= System.currentTimeMillis()
        return timeUp && linesRead(ctx) >= LINES_PER_PAGE
    }

    fun endSession(ctx: Context) {
        ctx.getSharedPreferences(SESSION, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
