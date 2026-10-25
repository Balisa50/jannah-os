package com.jannah.os.ramadan

import java.time.LocalDate

data class KhatmProjection(
    val totalPages: Int,
    val pagesRead: Int,
    val nightsRemaining: Int,
    val pagesAhead: Int,
    val nightlyTarget: Int
) {
    val isAhead: Boolean get() = pagesAhead >= 0
    val progressFraction: Float
        get() = if (totalPages == 0) 0f else pagesRead.toFloat() / totalPages.toFloat()
}

object TaraweehProjection {

    fun project(
        today: LocalDate,
        ramadanStart: LocalDate,
        pagesRead: Int,
        totalPages: Int = 604
    ): KhatmProjection {
        val daysElapsed = maxOf(0, java.time.temporal.ChronoUnit.DAYS.between(ramadanStart, today).toInt())
        val nightsRemaining = (30 - daysElapsed).coerceAtLeast(1)
        val expected = ((daysElapsed.toFloat() / 30f) * totalPages).toInt()
        val ahead = pagesRead - expected
        val target = ((totalPages - pagesRead).toFloat() / nightsRemaining.toFloat()).toInt().coerceAtLeast(1)
        return KhatmProjection(
            totalPages = totalPages,
            pagesRead = pagesRead,
            nightsRemaining = nightsRemaining,
            pagesAhead = ahead,
            nightlyTarget = target
        )
    }
}
