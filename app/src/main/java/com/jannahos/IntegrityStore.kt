package com.jannahos

import android.content.Context

object IntegrityStore {
    private const val PREFS = "jannah_integrity"

    // Focus Score starts at 100
    fun getFocusScore(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("focus_score", 100)

    fun deductFocus(ctx: Context, amount: Int) {
        val current = getFocusScore(ctx)
        val newScore = (current - amount).coerceAtLeast(0)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("focus_score", newScore).apply()
        
        // Log the bypass reason for the weekly report
        val bypasses = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("bypass_count", 0)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("bypass_count", bypasses + 1).apply()
    }

    fun restoreFocus(ctx: Context, amount: Int) {
        val current = getFocusScore(ctx)
        val newScore = (current + amount).coerceAtMost(100)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("focus_score", newScore).apply()
    }
}
