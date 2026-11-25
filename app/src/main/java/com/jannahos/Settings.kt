package com.jannahos

import android.content.Context

object Settings {
    private const val PREFS = "jannah_settings"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
    private const val KEY_METHOD = "method"

    fun lat(context: Context): Double = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_LAT, 0.0f).toDouble()
    fun lon(context: Context): Double = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_LON, 0.0f).toDouble()
    fun method(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_METHOD, "MWL") ?: "MWL"

    fun setMethod(context: Context, method: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_METHOD, method).apply()
    }

    fun set(context: Context, lat: Double, lon: Double, method: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .putString(KEY_METHOD, method).apply()
    }

    fun displayName(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("name", "") ?: ""
    fun setDisplayName(context: Context, name: String) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("name", name.trim()).apply() }

    fun onboardingComplete(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("onboarded", false)
    fun setOnboardingComplete(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("onboarded", true).apply() }

    fun setupComplete(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("setup_done", false)
    fun setSetupComplete(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("setup_done", true).apply() }

    fun getIqamahGap(ctx: Context): Int = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getInt("iqamah_gap", 15)
    fun setIqamahGap(ctx: Context, minutes: Int) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putInt("iqamah_gap", minutes).apply() }

    fun isTahajjudEnabled(ctx: Context): Boolean = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getBoolean("tahajjud_enabled", false)
    fun setTahajjudEnabled(ctx: Context, enabled: Boolean) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putBoolean("tahajjud_enabled", enabled).apply() }
    fun getTahajjudOffset(ctx: Context): Int = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getInt("tahajjud_offset", 60)
    fun setTahajjudOffset(ctx: Context, minutes: Int) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putInt("tahajjud_offset", minutes).apply() }

    fun isTahajjudSilent(ctx: Context): Boolean = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getBoolean("tahajjud_silent", false)
    fun setTahajjudSilent(ctx: Context, silent: Boolean) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putBoolean("tahajjud_silent", silent).apply() }

    fun isBedtimeLockEnabled(ctx: Context): Boolean = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getBoolean("bedtime_lock_enabled", false)
    fun setBedtimeLockEnabled(ctx: Context, enabled: Boolean) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putBoolean("bedtime_lock_enabled", enabled).apply() }

    fun isFaceDownEnabled(ctx: Context): Boolean = ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).getBoolean("face_down_enabled", false)
    fun setFaceDownEnabled(ctx: Context, enabled: Boolean) { ctx.getSharedPreferences("jannah_prefs", Context.MODE_PRIVATE).edit().putBoolean("face_down_enabled", enabled).apply() }
}
