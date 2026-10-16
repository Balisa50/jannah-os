package com.jannahos

import android.content.Context
import java.util.Calendar

// Decides the single most valuable action right now, based on:
// time of day, prayer status, tahajjud plan, and recent pattern.
// Returns ONE suggestion. Not a list. The user only sees one prompt.
object RightNow {

    data class Suggestion(
        val title: String,
        val body: String,
        val actionLabel: String?,   // null = no button, just information
        val actionRoute: String?,   // "pray" | "quran" | "dhikr" | "you" | null
        val kind: String
    )

    fun decide(context: Context, prayerTimes: Map<String, Long>): Suggestion? {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val today = SalahTracker.today(context)
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

        // Are we within 20 minutes of a prayer time?
        val imminent = ordered.firstOrNull { key ->
            val t = prayerTimes[key] ?: return@firstOrNull false
            val diff = t - now
            diff in -60_000L..20L * 60_000L && today[key] != true
        }
        if (imminent != null) {
            return Suggestion(
                title = "It is almost ${SalahTracker.label(imminent)}",
                body = "Prepare now. The window is short.",
                actionLabel = "Open prayer window",
                actionRoute = "pray",
                kind = "imminent_prayer"
            )
        }

        // Missed prayer earlier today?
        val missedEarlier = ordered.firstOrNull { key ->
            val t = prayerTimes[key] ?: return@firstOrNull false
            now > t + 30L * 60_000L && today[key] != true
        }
        if (missedEarlier != null) {
            return Suggestion(
                title = "${SalahTracker.label(missedEarlier)} is still pending",
                body = "You have not marked it yet. Return to it now, before the next one.",
                actionLabel = "Mark or pray now",
                actionRoute = "pray",
                kind = "pending_prayer"
            )
        }

        // Pre-dawn window — tahajjud opportunity
        if (hour in 2..5) {
            val inNight = TahajjudPlan.isOnTonight(context)
            return Suggestion(
                title = "The last third is now",
                body = if (inNight)
                    "Tonight is a tahajjud night. Two rak'ah. Do not let the moment pass."
                else
                    "Even on a rest night, two rak'ah now count. Allah descends. Ask.",
                actionLabel = "Open wake flow",
                actionRoute = "you",
                kind = "tahajjud"
            )
        }

        // Late evening — bed time
        if (hour in 22..23 || hour in 0..1) {
            val (bh, _) = BedtimeStore.get(context)
            if (hour >= bh || hour == 0 || hour == 1) {
                return Suggestion(
                    title = "Past your bedtime",
                    body = "Sleep now. The wake-up at tahajjud depends on this.",
                    actionLabel = "Tonight's plan",
                    actionRoute = "you",
                    kind = "bedtime"
                )
            }
        }

        // Mid-morning — dhikr opportunity
        if (hour in 9..11) {
            val istighfarToday = DhikrStore.countToday(context, "istighfar")
            if (istighfarToday < 10) {
                return Suggestion(
                    title = "Start your istighfar",
                    body = "You have said it $istighfarToday time${if (istighfarToday == 1) "" else "s"} today. The Prophet ﷺ sought forgiveness more than seventy times daily.",
                    actionLabel = "Open dhikr",
                    actionRoute = "dhikr",
                    kind = "dhikr"
                )
            }
        }

        // Afternoon — Quran
        if (hour in 14..16) {
            return Suggestion(
                title = "A page of Quran",
                body = "Ten minutes with the Book changes the shape of the day.",
                actionLabel = "Open Quran",
                actionRoute = "quran",
                kind = "quran"
            )
        }

        return null
    }
}
