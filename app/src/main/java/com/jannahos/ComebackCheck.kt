package com.jannahos

import android.content.Context

// On cold open, checks whether the user has been away or has stopped
// marking prayers. If so, returns a state the app renders as an honest
// elder-brother talk. Evidence-based, not generic.
object ComebackCheck {

    private const val PREFS = "jannah_comeback"
    private const val DAY_MS = 24L * 60 * 60 * 1000

    data class State(
        val show: Boolean,
        val daysAway: Int,
        val recentPrayers: Int,     // last 7 days
        val priorPrayers: Int,      // the 7 before that
        val headline: String,
        val body: String,
        val verse: String,
        val verseSource: String,
        val cta: String
    )

    fun check(context: Context): State {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastOpen = prefs.getLong("last_open", now)
        val daysAway = ((now - lastOpen) / DAY_MS).toInt().coerceAtLeast(0)
        prefs.edit().putLong("last_open", now).apply()

        val lastShown = prefs.getLong("last_shown", 0L)
        val daysSinceShown = ((now - lastShown) / DAY_MS).toInt()

        // Only speak if they have been gone long enough, and we have not
        // said this recently.
        if (daysAway < 4 || daysSinceShown < 3) return silent()

        val recent = SalahTracker.prayersLastNDays(context, 7, 0)
        val prior = SalahTracker.prayersLastNDays(context, 7, 7)
        val totalEver = prior + recent

        // Beginner or genuinely starting fresh — welcome, do not admonish.
        if (totalEver == 0) {
            prefs.edit().putLong("last_shown", now).apply()
            return State(
                show = true,
                daysAway = daysAway,
                recentPrayers = 0,
                priorPrayers = 0,
                headline = "You're back.",
                body = "Let's start simple. One prayer today. Not five — one. Then the next one.",
                verse = "\"And He it is who accepts repentance from His servants.\"",
                verseSource = "Qur'an 42:25",
                cta = "Open prayers"
            )
        }

        // Admonish — direct, but not cruel.
        val headline = when {
            daysAway >= 14 -> "$daysAway days. That's not nothing."
            daysAway >= 7 -> "A week. Almost."
            else -> "$daysAway days."
        }

        val body = buildString {
            if (prior > recent && prior >= 4) {
                append("You were praying. $prior prayers in a week. ")
                append("Then $recent. Something shifted. ")
                append("Not judging you. Just saying it out loud, because you wrote it down yourself.")
            } else if (recent == 0) {
                append("No prayers marked in a week. The obligation didn't change. ")
                append("You didn't change either — you're still the person who opened this.")
            } else {
                append("You've been off. The prayer is still fard. ")
                append("Nothing here is going to lecture. Just don't pretend it's fine.")
            }
        }

        prefs.edit().putLong("last_shown", now).apply()

        return State(
            show = true,
            daysAway = daysAway,
            recentPrayers = recent,
            priorPrayers = prior,
            headline = headline,
            body = body,
            verse = "\"Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah.\"",
            verseSource = "Qur'an 39:53",
            cta = "Pray one. Now."
        )
    }

    private fun silent() = State(false, 0, 0, 0, "", "", "", "", "")
}
