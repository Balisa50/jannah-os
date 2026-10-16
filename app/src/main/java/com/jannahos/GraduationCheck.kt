package com.jannahos

import android.content.Context

// After 90 days of data, if the habit is holding without needing the app's
// scaffolding, the app offers the user a Graduation, a respectful exit.
// This is not a feature that punishes; it is the ultimate respect.
// The data is never deleted. They can always return.
object GraduationCheck {

    data class Verdict(
        val ready: Boolean,
        val stats: WakeLog.FormationStats,
        val nightsConsidered: Int,
        val rate: Float,
        val reason: String
    )

    // Graduation criteria (deliberately generous, not strict):
    // - At least 60 nights of history
    // - At least 40% of those nights had a verified or claimed prayer
    // - The most recent 14 nights show no more than 3 misses
    fun evaluate(context: Context): Verdict {
        val log = WakeLog(context)
        val stats30 = log.formationStats(30)
        val stats90 = log.formationStats(90)
        val nights = stats90.verified + stats90.claimed
        val rate = if (stats90.total > 0) nights.toFloat() / stats90.total.toFloat() else 0f

        val recent = log.lastNights(14)
        val recentMisses = recent.count { it.outcome == "missed" }

        val enoughHistory = stats90.total >= 60
        val rateOk = rate >= 0.4f
        val recentOk = recentMisses <= 3

        val ready = enoughHistory && rateOk && recentOk

        val reason = when {
            !enoughHistory -> "Keep going. The picture becomes clear at 60 nights."
            !rateOk -> "You are still building. Return when the nights are steadier."
            !recentOk -> "The last two weeks show some misses. That is human. Not yet."
            else -> "Your practice holds without needing the app to prompt it."
        }

        return Verdict(ready, stats90, nights, rate, reason)
    }
}
