package com.jannahos

import android.content.Context

object BedtimeStore {
    private const val PREFS = "jannah_bedtime"
    private const val KEY_HOUR = "hour"
    private const val KEY_MIN = "min"

    fun set(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MIN, minute)
            .apply()
    }

    fun get(context: Context): Pair<Int, Int> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getInt(KEY_HOUR, 22) to p.getInt(KEY_MIN, 30)
    }
}
