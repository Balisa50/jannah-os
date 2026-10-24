package com.jannahos

import android.content.Context

object BedtimeStore {
    private const val PREFS = "bedtime_prefs"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_LOCK_ACTIVE = "lock_active"

    fun get(ctx: Context): Pair<Int, Int> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Pair(prefs.getInt(KEY_HOUR, 23), prefs.getInt(KEY_MINUTE, 0))
    }

    fun set(ctx: Context, hour: Int, minute: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }

    fun setLockActive(ctx: Context, active: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_LOCK_ACTIVE, active).apply()
    }

    fun isLockActive(ctx: Context): Boolean = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOCK_ACTIVE, false)
}
