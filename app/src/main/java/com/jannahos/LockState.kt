package com.jannahos

import android.content.Context

object LockState {
    private const val PREFS = "jannah_lock_state"
    private const val KEY_ENDS_AT = "ends_at"

    @Volatile var isLockActive: Boolean = false
    @Volatile private var endsAtMillis: Long = 0L

    fun restore(context: Context) {
        endsAtMillis = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ENDS_AT, 0L)
    }

    fun arm(context: Context, durationMs: Long) {
        endsAtMillis = System.currentTimeMillis() + durationMs
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_ENDS_AT, endsAtMillis).apply()
    }

    fun release(context: Context) {
        endsAtMillis = 0L
        isLockActive = false
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun remainingMs(): Long = (endsAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    fun isEnforcing(): Boolean = remainingMs() > 0L
}
