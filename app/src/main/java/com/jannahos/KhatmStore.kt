package com.jannahos

import android.content.Context

object KhatmStore {
    private const val PREFS = "khatm_prefs"
    private const val KEY_TARGET_DAYS = "target_days"
    private const val KEY_COMPLETED_DAYS = "completed_days"

    fun getTarget(ctx: Context): Int = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_TARGET_DAYS, 30)
    fun setTarget(ctx: Context, days: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_TARGET_DAYS, days).apply()
    }

    fun getCompleted(ctx: Context): Int = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_COMPLETED_DAYS, 0)
    fun markDayCompleted(ctx: Context) {
        val current = getCompleted(ctx)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_COMPLETED_DAYS, current + 1).apply()
    }

    fun reset(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_COMPLETED_DAYS, 0).apply()
    }

    fun getPagesForDay(ctx: Context): Pair<Int, Int> {
        val target = getTarget(ctx)
        val totalPages = 604
        val pagesPerDay = totalPages / target
        val completed = getCompleted(ctx)
        val start = completed * pagesPerDay + 1
        val end = if (completed == target - 1) totalPages else start + pagesPerDay - 1
        return Pair(start, end)
    }
}
