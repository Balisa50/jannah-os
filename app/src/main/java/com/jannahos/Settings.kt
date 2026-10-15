package com.jannahos

import android.content.Context

object Settings {
    private const val PREFS = "jannah_settings"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
    private const val KEY_METHOD = "method"

    fun lat(context: Context): Double =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_LAT, 13.4549f).toDouble()

    fun lon(context: Context): Double =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_LON, -16.5790f).toDouble()

    fun method(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_METHOD, "MWL") ?: "MWL"

    fun set(context: Context, lat: Double, lon: Double, method: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .putString(KEY_METHOD, method)
            .apply()
    }

    val METHODS = listOf(
        "MWL" to "Muslim World League",
        "EGYPT" to "Egyptian General Authority",
        "KARACHI" to "University of Islamic Sciences, Karachi",
        "MAKKAH" to "Umm al-Qura, Makkah",
        "ISNA" to "ISNA (North America)",
        "TEHRAN" to "Institute of Geophysics, Tehran",
        "GULF" to "Gulf Region"
    )
}
