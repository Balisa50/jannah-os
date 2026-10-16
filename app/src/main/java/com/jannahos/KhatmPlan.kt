package com.jannahos

import android.content.Context

// A reading plan to complete the entire Quran in 30, 60, or 90 days.
// Progress stored per-day. Missed days accumulate but never punish, the
// plan catches up by folding missed portions into the next reading.
object KhatmPlan {

    private const val PREFS = "jannah_khatm"

    data class Portion(
        val day: Int,
        val startSurah: Int,
        val startAyah: Int,
        val endSurah: Int,
        val endAyah: Int,
        val label: String
    )

    // Total ayahs in the Quran: 6236.
    private const val TOTAL_AYAHS = 6236

    // Cumulative ayah count at the START of each surah (index 0 = surah 1).
    // Values are cumulative: ayahs[0] = 0, ayahs[1] = 7 (end of Al-Fatihah), etc.
    private val CUMULATIVE = intArrayOf(
        0, 7, 293, 493, 669, 789, 954, 1160, 1235, 1364, 1473,
        1596, 1707, 1750, 1802, 1901, 2029, 2140, 2250, 2348, 2483,
        2595, 2673, 2791, 2855, 2932, 3159, 3252, 3340, 3409, 3469,
        3503, 3533, 3606, 3660, 3705, 3822, 3970, 4058, 4133, 4218,
        4272, 4325, 4414, 4473, 4510, 4545, 4583, 4612, 4630, 4675,
        4705, 4735, 4784, 4846, 4901, 4979, 5078, 5104, 5126, 5150,
        5163, 5177, 5188, 5188, 5206, 5222, 5272, 5323, 5375, 5419,
        5447, 5475, 5495, 5551, 5591, 5622, 5672, 5712, 5758, 5800,
        5834, 5867, 5899, 5925, 5967, 5993, 6023, 6043, 6058, 6088,
        6125, 6130, 6138, 6146, 6157, 6168, 6176, 6188, 6193, 6197,
        6204, 6209, 6214, 6218, 6221, 6225, 6229, 6233, 6236, 6236,
        6236, 6236, 6236, 6236, 6236
    )

    // Returns the surah:ayah for a global ayah number (1..6236).
    private fun locate(globalAyah: Int): Pair<Int, Int> {
        val g = globalAyah.coerceIn(1, TOTAL_AYAHS)
        var surah = 1
        for (i in 1..114) {
            if (CUMULATIVE[i] >= g) {
                surah = i
                break
            }
        }
        val start = CUMULATIVE[surah - 1]
        val ayahInSurah = g - start
        return surah to ayahInSurah
    }

    fun planForDays(days: Int): List<Portion> {
        val perDay = TOTAL_AYAHS.toDouble() / days.toDouble()
        val out = mutableListOf<Portion>()
        for (d in 0 until days) {
            val startG = (d * perDay).toInt() + 1
            val endG = ((d + 1) * perDay).toInt().coerceAtMost(TOTAL_AYAHS)
            val (sS, sA) = locate(startG)
            val (eS, eA) = locate(endG)
            val label = if (sS == eS)
                "Surah $sS, ayah $sA–$eA"
            else
                "Surah $sS:$sA → Surah $eS:$eA"
            out.add(Portion(d + 1, sS, sA, eS, eA, label))
        }
        return out
    }

    // --- Persistence ---

    fun planDays(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("days", 0)

    fun setPlan(context: Context, days: Int) {
        val start = System.currentTimeMillis()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt("days", days)
            .putLong("start", start)
            .apply()
    }

    fun startDate(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("start", 0L)

    fun completedDays(context: Context): Set<Int> {
        val csv = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("done", "") ?: ""
        return csv.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleDay(context: Context, day: Int) {
        val current = completedDays(context).toMutableSet()
        if (!current.add(day)) current.remove(day)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("done", current.joinToString(",")).apply()
    }

    // The next unread day = first day not in completed set.
    fun nextDay(context: Context): Int? {
        val days = planDays(context)
        if (days == 0) return null
        val done = completedDays(context)
        for (i in 1..days) if (i !in done) return i
        return null
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
