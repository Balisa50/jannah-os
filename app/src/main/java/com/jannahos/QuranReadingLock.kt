package com.jannahos

import android.content.Context

object QuranReadingLock {
    private const val PREFS = "jannah_quran_lock"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_DONE_DAY = "done_day"

    const val DEFAULT_MS = 10 * 60_000L
    const val LINES_PER_PAGE = 15

    fun enabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, on).apply()
    }

    fun isDoneToday(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DONE_DAY, "") == WakeLog.todayStamp()

    fun markDoneToday(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_DONE_DAY, WakeLog.todayStamp()).apply()
    }

    fun shouldLock(ctx: Context): Boolean = enabled(ctx) && !isDoneToday(ctx)

    // Remaining time is tracked in memory via the screen, but saved here
    fun getSavedRemainingMs(ctx: Context): Long =
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).getLong("remaining", DEFAULT_MS)

    fun saveRemainingMs(ctx: Context, ms: Long) {
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).edit().putLong("remaining", ms).apply()
    }

    fun incrementLines(ctx: Context, delta: Int) {
        val p = ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE)
        p.edit().putInt("lines", p.getInt("lines", 0) + delta).apply()
    }

    fun linesRead(ctx: Context): Int =
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).getInt("lines", 0)

    fun isSatisfied(ctx: Context): Boolean = linesRead(ctx) >= LINES_PER_PAGE

    fun resetSession(ctx: Context) {
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).edit()
            .putLong("remaining", DEFAULT_MS)
            .putInt("lines", 0)
            .apply()
    }
}
