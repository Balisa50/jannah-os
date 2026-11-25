package com.jannahos

import android.content.Context

object BedtimeStore {
    private const val PREFS = "bedtime_prefs"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_LOCK_ACTIVE = "lock_active"
    private const val KEY_LOCK_ENDS_AT = "lock_ends_at"

    fun get(ctx: Context): Pair<Int, Int> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Pair(prefs.getInt(KEY_HOUR, 23), prefs.getInt(KEY_MINUTE, 0))
    }

    fun set(ctx: Context, hour: Int, minute: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun arm(ctx: Context, durationMs: Long) {
        val safeDuration = durationMs.coerceIn(60_000L, 24 * 60 * 60 * 1000L)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOCK_ACTIVE, true)
            .putLong(KEY_LOCK_ENDS_AT, System.currentTimeMillis() + safeDuration)
            .apply()
    }

    fun release(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOCK_ACTIVE, false)
            .remove(KEY_LOCK_ENDS_AT)
            .apply()
    }

    fun remainingMs(ctx: Context): Long {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_LOCK_ACTIVE, false)) return 0L
        val remaining = (prefs.getLong(KEY_LOCK_ENDS_AT, 0L) - System.currentTimeMillis())
            .coerceAtLeast(0L)
        if (remaining == 0L) release(ctx)
        return remaining
    }

    fun isLockActive(ctx: Context): Boolean = remainingMs(ctx) > 0L

    // Kept for compatibility with existing callers.
    fun setLockActive(ctx: Context, active: Boolean) {
        if (active) arm(ctx, 8 * 60 * 60 * 1000L) else release(ctx)
    }
}
