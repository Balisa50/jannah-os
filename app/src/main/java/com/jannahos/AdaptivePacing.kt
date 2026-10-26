package com.jannahos

import java.util.concurrent.TimeUnit

object AdaptivePacing {

    data class PacingResult(
        val pagesToday: Int,
        val expectedProgress: Int,
        val actualProgress: Int,
        val isBehind: Boolean,
        val isAhead: Boolean,
        val message: String
    )

    /**
     * Calculates the dynamic daily reading target.
     * @param totalPages Total pages in Quran (604)
     * @param totalDays The user's chosen pace (e.g. 30 days)
     * @param daysElapsed How many days since they started
     * @param pagesCompleted How many pages they've actually read
     */
    fun calculatePacing(totalPages: Int, totalDays: Int, daysElapsed: Int, pagesCompleted: Int): PacingResult {
        val daysRemaining = (totalDays - daysElapsed).coerceAtLeast(1)
        val pagesRemaining = (totalPages - pagesCompleted).coerceAtLeast(0)

        // How many pages should they have read by now?
        val expectedProgress = ((daysElapsed.toFloat() / totalDays.toFloat()) * totalPages).toInt()
        
        val isBehind = pagesCompleted < (expectedProgress - 5)
        val isAhead = pagesCompleted > (expectedProgress + 5)

        // The core algorithm: Spread the remaining pages over the remaining days
        val pagesToday = (pagesRemaining.toFloat() / daysRemaining.toFloat()).let { 
            if (it <= 0f) 0 else Math.ceil(it.toDouble()).toInt() 
        }

        val message = when {
            pagesCompleted >= totalPages -> "Khatm complete. May Allah accept it."
            isBehind -> "You fell behind. Adjusted target is $pagesToday pages/day to catch up gently."
            isAhead -> "You are ahead of schedule. Keep going at $pagesToday pages/day."
            else -> "You are exactly on pace. Read $pagesToday pages today."
        }

        return PacingResult(pagesToday, expectedProgress, pagesCompleted, isBehind, isAhead, message)
    }
}
