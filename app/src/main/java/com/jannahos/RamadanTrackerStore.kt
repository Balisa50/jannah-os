package com.jannahos

import android.content.Context

object RamadanTrackerStore {
    private const val PREFS = "ramadan_tracker"
    
    // Laylatul Qadr tracking (Nights 21-29)
    fun isNightPrayed(ctx: Context, night: Int): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("night_$night", false)
        
    fun setNightPrayed(ctx: Context, night: Int, prayed: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("night_$night", prayed).apply()
    }

    fun prayedNightsCount(ctx: Context): Int {
        var count = 0
        for (i in 21..29 step 2) { // Odd nights
            if (isNightPrayed(ctx, i)) count++
        }
        return count
    }

    // Missed Fasts tracking
    fun getMissedFasts(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("missed_fasts", 0)
        
    fun addMissedFast(ctx: Context) {
        val current = getMissedFasts(ctx)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("missed_fasts", current + 1).apply()
    }
    
    fun removeMissedFast(ctx: Context) {
        val current = getMissedFasts(ctx)
        if (current > 0) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("missed_fasts", current - 1).apply()
        }
    }
}
