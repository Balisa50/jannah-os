package com.jannahos

import android.content.Context

object SadaqahStore {
    private const val PREFS = "sadaqah_prefs"
    private const val KEY_DEBT = "charity_debt"

    fun getCurrency(ctx: Context): String {
        val country = CityStore.country(ctx).uppercase()
        return when {
            country.contains("GAMBIA") -> "D"
            country.contains("NIGERIA") -> "₦"
            country.contains("EGYPT") -> "E£"
            country.contains("SAUDI") -> "SR"
            country.contains("UAE") -> "AED"
            country.contains("UK") || country.contains("UNITED KINGDOM") -> "£"
            country.contains("EUROPE") -> "€"
            else -> "$"
        }
    }

    fun getDebt(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_DEBT, 0)

    fun addPenalty(ctx: Context, amount: Int = 1) {
        val current = getDebt(ctx)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_DEBT, current + amount).apply()
    }

    fun clearDebt(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_DEBT, 0).apply()
    }
}
