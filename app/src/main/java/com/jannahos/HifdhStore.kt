package com.jannahos

import android.content.Context

object HifdhStore {
    private const val PREFS = "hifdh_prefs"
    private const val KEY_MEMORIZED = "memorized_surahs"

    fun getMemorized(ctx: Context): Set<Int> {
        val set = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_MEMORIZED, emptySet()) ?: emptySet()
        return set.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setMemorized(ctx: Context, surahId: Int, isMemorized: Boolean) {
        val current = getMemorized(ctx).toMutableSet()
        if (isMemorized) current.add(surahId) else current.remove(surahId)
        val stringSet = current.map { it.toString() }.toSet()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_MEMORIZED, stringSet).apply()
    }
}
