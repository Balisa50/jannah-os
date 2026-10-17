package com.jannahos

import android.content.Context

// The reading plan. The user chooses how many days they want to finish
// the Quran in. We divide the 6236 ayahs evenly. Each day's portion is
// given as a precise surah:ayah range.
object KhatmPlan {

    private const val PREFS = "jannah_khatm"
    private const val TOTAL_AYAHS = 6236

    // Exact ayah count for each of the 114 surahs in the Uthmani mushaf.
    private val AYAH_COUNTS = intArrayOf(
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
        123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
        112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
        34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
        54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
        60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
        28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
        15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
        11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
        5, 4, 5, 6
    )

    // Cumulative ayah count AT THE START of each surah.
    // cumulativeStarts[0] = 0 (start of surah 1)
    // cumulativeStarts[1] = 7 (start of surah 2)
    // ... cumulativeStarts[113] = 6230 (start of surah 114)
    private val CUMULATIVE_STARTS: IntArray = IntArray(114).also { starts ->
        var sum = 0
        for (i in 0 until 114) {
            starts[i] = sum
            sum += AYAH_COUNTS[i]
        }
    }

    data class Portion(
        val day: Int,
        val startSurah: Int,
        val startAyah: Int,
        val endSurah: Int,
        val endAyah: Int,
        val label: String,
        val ayahCount: Int
    )

    // Given a global ayah number (1..6236), return the (surah, ayah).
    private fun locate(globalAyah: Int): Pair<Int, Int> {
        val g = globalAyah.coerceIn(1, TOTAL_AYAHS)
        var surah = 1
        for (i in 0 until 114) {
            if (CUMULATIVE_STARTS[i] + AYAH_COUNTS[i] >= g) {
                surah = i + 1
                break
            }
        }
        val ayahInSurah = g - CUMULATIVE_STARTS[surah - 1]
        return surah to ayahInSurah
    }

    // Build the plan for exactly `days` days.
    // Day 1 starts at ayah 1 (Al-Fatihah 1).
    // Day N ends at ayah 6236 (An-Nas 6).
    // Everything in between is divided evenly.
    fun planForDays(days: Int): List<Portion> {
        val safeDays = days.coerceIn(1, 365)
        val perDay = TOTAL_AYAHS.toDouble() / safeDays.toDouble()
        val out = mutableListOf<Portion>()

        for (d in 0 until safeDays) {
            val startG = (d * perDay).toInt() + 1
            val endG = if (d == safeDays - 1) TOTAL_AYAHS
                       else ((d + 1) * perDay).toInt().coerceAtMost(TOTAL_AYAHS)

            val (sS, sA) = locate(startG)
            val (eS, eA) = locate(endG)

            val label = if (sS == eS)
                "${surahName(sS)} $sA-$eA"
            else
                "${surahName(sS)} $sA → ${surahName(eS)} $eA"

            out.add(
                Portion(
                    day = d + 1,
                    startSurah = sS,
                    startAyah = sA,
                    endSurah = eS,
                    endAyah = eA,
                    label = label,
                    ayahCount = endG - startG + 1
                )
            )
        }
        return out
    }

    private fun surahName(surah: Int): String =
        QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"

    // ---------- Persistence ----------

    fun planDays(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("days", 0)

    fun setPlan(context: Context, days: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt("days", days.coerceIn(1, 365))
            .putLong("start", System.currentTimeMillis())
            .putString("done", "")
            .apply()
    }

    fun startDate(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("start", 0L)

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

    // The first day not yet marked done.
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
