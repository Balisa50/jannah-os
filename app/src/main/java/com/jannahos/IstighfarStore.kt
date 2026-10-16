package com.jannahos

import android.content.Context

object IstighfarStore {
    private const val PREFS = "jannah_istighfar"
    private const val KEY_DAY = "day"
    private const val KEY_COUNT = "count"

    fun todayCount(context: Context): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedDay = p.getString(KEY_DAY, "")
        val today = WakeLog.todayStamp()
        if (storedDay != today) return 0
        return p.getInt(KEY_COUNT, 0)
    }

    fun increment(context: Context): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val storedDay = p.getString(KEY_DAY, "")
        val current = if (storedDay == today) p.getInt(KEY_COUNT, 0) else 0
        val next = current + 1
        p.edit().putString(KEY_DAY, today).putInt(KEY_COUNT, next).apply()
        return next
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
