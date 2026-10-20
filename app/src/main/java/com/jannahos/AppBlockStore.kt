package com.jannahos

import android.content.Context

object AppBlockStore {
    private const val PREFS = "app_block_prefs"
    private const val KEY_BLOCKED = "blocked_apps"
    private const val KEY_BLOCK_ALWAYS = "block_always"

    fun blockedPackages(ctx: Context): Set<String> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_BLOCKED, emptySet()) ?: emptySet()

    fun setBlocked(ctx: Context, packages: Set<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_BLOCKED, packages).apply()
    }

    fun count(ctx: Context): Int = blockedPackages(ctx).size

    fun isBlocked(ctx: Context, pkg: String): Boolean = blockedPackages(ctx).contains(pkg)

    // New: Blocking Schedule
    fun blockAlways(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_BLOCK_ALWAYS, false)

    fun setBlockAlways(ctx: Context, always: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_BLOCK_ALWAYS, always).apply()
    }
}
