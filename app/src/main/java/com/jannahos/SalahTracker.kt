package com.jannahos

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SalahTracker {
    private const val PREFS = "jannah_salah"
    private const val HISTORY = "jannah_salah_history"
    private val PRAYERS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    fun today(context: Context): Map<String, Boolean> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val stored = p.getString("day", "")
        if (stored != today) {
            p.edit().putString("day", today)
                .putBoolean("fajr", false)
                .putBoolean("dhuhr", false)
                .putBoolean("asr", false)
                .putBoolean("maghrib", false)
                .putBoolean("isha", false)
                .apply()
        }
        return PRAYERS.associateWith { p.getBoolean(it, false) }
    }

    fun toggle(context: Context, prayer: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val stored = p.getString("day", "")
        if (stored != today) p.edit().putString("day", today).apply()
        val current = p.getBoolean(prayer, false)
        val next = !current
        p.edit().putBoolean(prayer, next).apply()

        // Persist to history so we can talk honestly about patterns later.
        val h = context.getSharedPreferences(HISTORY, Context.MODE_PRIVATE).edit()
        if (next) h.putBoolean("${today}_$prayer", true)
        else h.remove("${today}_$prayer")
        h.apply()
    }

    fun prayersOn(context: Context, day: String): Int {
        val p = context.getSharedPreferences(HISTORY, Context.MODE_PRIVATE)
        return PRAYERS.count { p.getBoolean("${day}_$it", false) }
    }

    // Count prayers over the last N days starting from an offset (in days back).
    fun prayersLastNDays(context: Context, n: Int, offsetDays: Int = 0): Int {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays)
        var total = 0
        for (i in 0 until n) {
            total += prayersOn(context, fmt.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return total
    }

    fun label(prayer: String): String = when (prayer) {
        "fajr" -> "Fajr"
        "dhuhr" -> "Dhuhr"
        "asr" -> "Asr"
        "maghrib" -> "Maghrib"
        "isha" -> "Isha"
        else -> prayer
    }
}
