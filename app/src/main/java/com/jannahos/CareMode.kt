package com.jannahos

import android.content.Context

// When the diary shows a persistent pattern of hardship (sadness, struggle,
// anxiety, or repeated sin language) across multiple entries in the recent
// window, the app enters Care Mode. This changes how it speaks to you:
// - No streak or count shown
// - No "level up" prompts
// - Mercy and dua surface before anything else
// - The formation plan quietly lowers its expectation
// - When the pattern clears, Care Mode turns off on its own
object CareMode {

    // How many "heavy" diary entries within the last 10 days flip the switch.
    private const val THRESHOLD = 3
    private const val WINDOW_DAYS = 10

    private val HEAVY_TAGS = setOf(
        "struggle_sin", "struggle_lying", "struggle_speech",
        "struggle_parents", "struggle_prayer",
        "feeling_sadness", "feeling_anxiety"
    )

    data class State(
        val active: Boolean,
        val heavyCount: Int,
        val reason: String
    )

    fun evaluate(context: Context): State {
        val entries = try { DiaryStore(context).recent(50) } catch (e: Exception) { emptyList() }
        val cutoff = System.currentTimeMillis() - WINDOW_DAYS.toLong() * 24 * 60 * 60 * 1000
        val heavy = entries.filter { it.ts >= cutoff && it.tag in HEAVY_TAGS }
        val active = heavy.size >= THRESHOLD

        val reason = when {
            !active -> "Everyday mode."
            heavy.any { it.tag.startsWith("struggle") } ->
                "Recent entries suggest a difficult season. The app has softened."
            else ->
                "You have been carrying heaviness. The app has softened."
        }

        return State(active, heavy.size, reason)
    }

    // A message shown once per session on the Today screen when Care Mode is on.
    fun message(): Pair<String, String> = "A gentler day" to
        "You're carrying something. Nothing here is urgent."

    // In Care Mode, we cap the formation suggestion to one night/week
    // and hide any "level up" prompt. Not a permanent change, just for the season.
    const val CAVEAT = "This passes."
}
