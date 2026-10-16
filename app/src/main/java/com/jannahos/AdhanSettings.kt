package com.jannahos

import android.content.Context

object AdhanSettings {
    private const val PREFS = "jannah_adhan"

    // Three options: a bundled short adhan tone, system default notification, or silent.
    data class Option(val id: String, val label: String)

    val options = listOf(
        Option("adhan_short", "Adhan tone"),
        Option("system", "System notification"),
        Option("silent", "Silent, vibrate only")
    )

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("id", "adhan_short") ?: "adhan_short"

    fun set(context: Context, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("id", id).apply()
    }
}
