package com.jannahos

import android.content.Context

object RegretStore {
    private const val PREFS = "regret_store"
    private const val KEY_DATE = "current_date"
    private const val KEY_PREFIX = "blocked_"

    private fun today(): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    private fun checkAndReset(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "") ?: ""
        if (savedDate != today()) {
            // New day, clear the logs
            val editor = prefs.edit()
            for (key in prefs.all.keys) {
                if (key.startsWith(KEY_PREFIX)) editor.remove(key)
            }
            editor.putString(KEY_DATE, today())
            editor.apply()
        }
    }

    fun logAttempt(ctx: Context, packageName: String) {
        checkAndReset(ctx)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_PREFIX + packageName, 0)
        prefs.edit().putInt(KEY_PREFIX + packageName, current + 1).apply()
    }

    fun getTopRegret(ctx: Context): Pair<String, Int>? {
        checkAndReset(ctx)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var topPkg: String? = null
        var maxCount = 0

        for ((key, value) in prefs.all) {
            if (key.startsWith(KEY_PREFIX) && value is Int) {
                if (value > maxCount) {
                    maxCount = value
                    topPkg = key.removePrefix(KEY_PREFIX)
                }
            }
        }
        return if (topPkg != null && maxCount > 0) Pair(topPkg, maxCount) else null
    }
}
