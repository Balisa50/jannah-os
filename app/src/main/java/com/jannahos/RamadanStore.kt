package com.jannahos

import android.content.Context

object RamadanStore {
    private const val PREFS = "ramadan_prefs"
    private const val KEY_FASTED = "fasted_"

    fun isFastedToday(ctx: Context): Boolean {
        val today = WakeLog.todayStamp()
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_FASTED + today, false)
    }

    fun setFastedToday(ctx: Context, fasted: Boolean) {
        val today = WakeLog.todayStamp()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_FASTED + today, fasted).apply()
    }
}
