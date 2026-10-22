package com.jannahos

import android.content.Context

object QuranReadingLock {
    private const val PREFS = "jannah_quran_lock"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_DONE_DAY = "done_day"
    private const val KEY_ACTIVE_TODAY = "active_today"

    const val DEFAULT_MS = 5 * 60_000L
    const val LINES_PER_PAGE = 15

    fun enabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, on).apply()
        if (on) {
            // Schedule the random lock for today
            QuranLockScheduler.scheduleRandomLock(ctx)
        } else {
            setActiveToday(ctx, false)
        }
    }

    fun isDoneToday(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DONE_DAY, "") == WakeLog.todayStamp()

    fun markDoneToday(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_DONE_DAY, WakeLog.todayStamp()).apply()
        setActiveToday(ctx, false)
    }

    fun isActiveToday(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVE_TODAY, false)

    fun setActiveToday(ctx: Context, active: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ACTIVE_TODAY, active).apply()
    }

    fun shouldLock(ctx: Context): Boolean = enabled(ctx) && isActiveToday(ctx) && !isDoneToday(ctx)

    fun getSavedRemainingMs(ctx: Context): Long {
        val ms = ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).getLong("remaining", DEFAULT_MS)
        return if (ms <= 0L) DEFAULT_MS else ms
    }

    fun saveRemainingMs(ctx: Context, ms: Long) {
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).edit().putLong("remaining", ms).apply()
    }

    fun getLastSelectedSurah(ctx: Context): Int =
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).getInt("last_surah", 1)

    fun setLastSelectedSurah(ctx: Context, surah: Int) {
        ctx.getSharedPreferences("lock_session", Context.MODE_PRIVATE).edit().putInt("last_surah", surah).apply()
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
