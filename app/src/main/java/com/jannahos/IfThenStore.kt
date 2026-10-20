package com.jannahos

import android.content.Context

object IfThenStore {
    private const val PREFS = "if_then_prefs"
    private const val KEY_PLANS = "plans"

    fun getPlans(ctx: Context): List<Pair<String, String>> {
        val set = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_PLANS, emptySet()) ?: emptySet()
        return set.mapNotNull {
            val parts = it.split("|||")
            if (parts.size == 2) Pair(parts[0], parts[1]) else null
        }
    }

    fun addPlan(ctx: Context, trigger: String, action: String) {
        val current = getPlans(ctx).toMutableList()
        current.add(Pair(trigger, action))
        val set = current.map { "${it.first}|||${it.second}" }.toSet()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_PLANS, set).apply()
    }

    fun removePlan(ctx: Context, trigger: String, action: String) {
        val current = getPlans(ctx).toMutableList()
        current.remove(Pair(trigger, action))
        val set = current.map { "${it.first}|||${it.second}" }.toSet()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_PLANS, set).apply()
    }
}
