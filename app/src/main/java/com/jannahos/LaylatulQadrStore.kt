package com.jannahos

import android.content.Context

object LaylatulQadrStore {
    private const val PREFS = "jannah_laylatul_qadr"
    private const val KEY_PREFIX = "night_"

    fun isPrayed(ctx: Context, night: Int): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_PREFIX + night, false)

    fun setPrayed(ctx: Context, night: Int, prayed: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PREFIX + night, prayed).apply()
    }

    fun prayedNights(ctx: Context): Set<Int> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (21..30).filter { prefs.getBoolean(KEY_PREFIX + it, false) }.toSet()
    }

    fun prayedCount(ctx: Context): Int = prayedNights(ctx).size
}
