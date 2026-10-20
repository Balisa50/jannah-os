package com.jannahos

import android.content.Context

object HifdhStore {
    private const val PREFS = "hifdh_prefs"
    private const val KEY_MEMORIZED = "memorized_surahs"
    private const val KEY_NEXT_REVIEW = "next_review_"
    private const val KEY_INTERVAL = "interval_"

    fun getMemorized(ctx: Context): Set<Int> {
        val set = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_MEMORIZED, emptySet()) ?: emptySet()
        return set.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setMemorized(ctx: Context, surahId: Int, isMemorized: Boolean) {
        val current = getMemorized(ctx).toMutableSet()
        if (isMemorized) {
            current.add(surahId)
            // Initialize review schedule for today
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(KEY_NEXT_REVIEW + surahId, System.currentTimeMillis())
                .putInt(KEY_INTERVAL + surahId, 1)
                .apply()
        } else {
            current.remove(surahId)
        }
        val stringSet = current.map { it.toString() }.toSet()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_MEMORIZED, stringSet).apply()
    }

    fun getNextReview(ctx: Context, surahId: Int): Long =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_NEXT_REVIEW + surahId, 0L)

    fun getInterval(ctx: Context, surahId: Int): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_INTERVAL + surahId, 1)

    fun review(ctx: Context, surahId: Int, passed: Boolean) {
        val currentInterval = getInterval(ctx, surahId)
        // Spaced repetition: Double if passed, reset to 1 if failed
        val newInterval = if (passed) currentInterval * 2 else 1
        val nextReview = System.currentTimeMillis() + (newInterval * 24L * 60 * 60 * 1000)
        
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_INTERVAL + surahId, newInterval)
            .putLong(KEY_NEXT_REVIEW + surahId, nextReview)
            .apply()
    }

    fun getDueSurahs(ctx: Context): List<Int> {
        val memorized = getMemorized(ctx)
        val now = System.currentTimeMillis()
        return memorized.filter { getNextReview(ctx, it) <= now }.sorted()
    }
}
