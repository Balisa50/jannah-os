package com.jannahos

import android.content.Context

// User-selected set of package names to lock during prayer windows.
// Nothing is locked by default. The user chooses from a picker.
object AppBlockStore {
    private const val PREFS = "jannah_app_block"

    fun blockedPackages(context: Context): Set<String> {
        val csv = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("set", "") ?: ""
        if (csv.isBlank()) return emptySet()
        return csv.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun setBlocked(context: Context, packages: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("set", packages.joinToString(",")).apply()
    }

    fun toggle(context: Context, pkg: String): Boolean {
        val current = blockedPackages(context).toMutableSet()
        val added = current.add(pkg)
        if (!added) current.remove(pkg)
        setBlocked(context, current)
        return added
    }

    fun isBlocked(context: Context, pkg: String): Boolean =
        blockedPackages(context).contains(pkg)

    fun count(context: Context): Int = blockedPackages(context).size

    // Default suggestions — the ones most users want to block first.
    val SUGGESTED = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",       // TikTok
        "com.twitter.android",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.google.android.youtube",
        "com.reddit.frontpage",
        "com.whatsapp"
    )
}
