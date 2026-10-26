package com.jannahos

import android.content.Context
import java.util.concurrent.TimeUnit

object KhatmStore {
    private const val PREFS = "khatm_prefs"
    private const val KEY_TARGET_DAYS = "target_days"
    private const val KEY_COMPLETED_DAYS = "completed_days"
    private const val KEY_START_DATE = "start_date"

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
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_COMPLETED_DAYS, 0)
            .putLong(KEY_START_DATE, System.currentTimeMillis())
            .apply()
    }

    fun getStartDate(ctx: Context): Long = 
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_START_DATE, System.currentTimeMillis())

    fun getDaysElapsed(ctx: Context): Int {
        val start = getStartDate(ctx)
        val diff = System.currentTimeMillis() - start
        return TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
    }

    fun getRangeForDay(ctx: Context, dayIndex: Int): Pair<Int, Int> {
        val target = getTarget(ctx)
        val start = (dayIndex * 604 / target) + 1
        val end = ((dayIndex + 1) * 604 / target)
        return Pair(start, end)
    }
}
