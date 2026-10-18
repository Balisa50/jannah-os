package com.jannahos

import android.content.Context

object AppBlockStore {
    private const val PREFS = "app_block_prefs"
    private const val KEY_BLOCKED = "blocked_apps"

    fun blockedPackages(ctx: Context): Set<String> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_BLOCKED, emptySet()) ?: emptySet()

    fun setBlocked(ctx: Context, packages: Set<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_BLOCKED, packages).apply()
    }

    fun count(ctx: Context): Int = blockedPackages(ctx).size

    fun isBlocked(ctx: Context, pkg: String): Boolean = blockedPackages(ctx).contains(pkg)
}
