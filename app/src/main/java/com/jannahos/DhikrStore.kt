package com.jannahos

import android.content.Context

// Per-dhikr, per-day counter. Resets on day roll-over, like the istighfar.
object DhikrStore {
    private const val PREFS = "jannah_dhikr_v2"

    fun countToday(context: Context, id: String): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedDay = p.getString("${id}_day", "")
        val today = WakeLog.todayStamp()
        if (storedDay != today) return 0
        return p.getInt(id, 0)
    }

    fun increment(context: Context, id: String): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val storedDay = p.getString("${id}_day", "")
        val current = if (storedDay == today) p.getInt(id, 0) else 0
        val next = current + 1
        p.edit().putString("${id}_day", today).putInt(id, next).apply()
        return next
    }

    fun reset(context: Context, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(id).remove("${id}_day").apply()
    }
}
