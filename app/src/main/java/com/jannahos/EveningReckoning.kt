package com.jannahos

import android.content.Context
import java.util.Calendar

// Once-per-day evening reflection. Reads everything the user did today
// across the app and returns a single compassionate summary. No score,
// no streak, no ranking. Just an honest look back.
object EveningReckoning {

    private const val PREFS = "jannah_evening"

    data class Summary(
        val prayersMarked: Int,
        val prayersTotal: Int,
        val dhikrTotal: Int,
        val quranRead: Boolean,
        val gifted: Boolean,
        val fasted: Boolean,
        val diaryWritten: Boolean,
        val lines: List<String>,   // compassionate observations
        val closing: String
    )

    // Only show between 20:00 and 23:59 if not already seen today.
    fun shouldShow(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (p.getString("day", "") == WakeLog.todayStamp()) return false
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 20..23
    }

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("day", WakeLog.todayStamp()).apply()
    }

    fun summarise(context: Context): Summary {
        val prayers = SalahTracker.today(context)
        val prayersMarked = prayers.count { it.value }

        val dhikrTotal = DhikrStore.countToday(context, "istighfar") +
            DhikrStore.countToday(context, "subhanallah") +
            DhikrStore.countToday(context, "alhamdulillah") +
            DhikrStore.countToday(context, "allahuakbar") +
            DhikrStore.countToday(context, "tahlil") +
            DhikrStore.countToday(context, "hawqala") +
            DhikrStore.countToday(context, "salawat")

        val quranRead = QuranBookmarks.lastSurah(context) != null

        val gifted = try { SadaqahLog(context).countLast30() > 0 } catch (e: Exception) { false }
        val fasted = try { FastLog(context).todayKind() != null } catch (e: Exception) { false }
        val diaryWritten = try {
            DiaryStore(context).latestTagIn24h() != null
        } catch (e: Exception) { false }

        // Compassionate lines, only mention what actually happened
        val lines = mutableListOf<String>()
        if (prayersMarked == 5) lines.add("All five prayers marked. A complete day.")
        else if (prayersMarked >= 3) lines.add("$prayersMarked prayers marked. Steady.")
        else if (prayersMarked > 0) lines.add("$prayersMarked prayers marked. Beginning counts.")
        else lines.add("Prayer went unmarked today. Tomorrow is a fresh page.")

if (quranRead) lines.add("Quran opened. The Book was in your hands today.")
        if (gifted) lines.add("You gave. The Prophet  said charity does not decrease wealth.")
        if (fasted) lines.add("You fasted. Allah says: fasting is for Me and I will reward it.")
        if (diaryWritten) lines.add("You wrote. What you write, you own.")

        if (lines.isEmpty()) lines.add("A quiet day. The app noticed you were here.")

        val closing = when {
            prayersMarked == 5 && quranRead && dhikrTotal > 0 ->
                "Sleep with intention. Tomorrow begins tonight."
            prayersMarked >= 3 ->
                "Good enough is not a compromise. It is progress."
            prayersMarked > 0 ->
                "Return tomorrow. The door is always open."
            else ->
                "Sleep. Wake tomorrow. Begin again, without guilt."
        }

        return Summary(
            prayersMarked = prayersMarked,
            prayersTotal = 5,
            dhikrTotal = dhikrTotal,
            quranRead = quranRead,
            gifted = gifted,
            fasted = fasted,
            diaryWritten = diaryWritten,
            lines = lines,
            closing = closing
        )
    }
}
