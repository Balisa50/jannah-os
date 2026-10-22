package com.jannahos

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

// Computes upcoming Islamic dates from the Hijri calendar.
// Dates can differ by a day depending on moon sighting, we flag this.
object IslamicCalendar {

    data class Event(
        val name: String,
        val gregorianDate: LocalDate,
        val hijriLabel: String,
        val daysAway: Long,
        val kind: String,
        val note: String
    )

    private val FIXED_EVENTS = listOf(
        Triple(1, 1, "Muharram, new year"),
        Triple(1, 10, "Ashura (recommended fast)"),
        Triple(1, 9, "Tasu'a (day before Ashura)"),
        Triple(3, 12, "Mawlid, scholarly differences exist"),
        Triple(9, 1, "Ramadan begins"),
        Triple(9, 27, "Laylat al-Qadr, most likely of the odd nights"),
        Triple(10, 1, "Eid al-Fitr"),
        Triple(12, 9, "Day of Arafah (recommended fast)"),
        Triple(12, 10, "Eid al-Adha"),
        Triple(12, 11, "Days of Tashriq begin")
    )

    fun upcoming(withinDays: Int = 400): List<Event> {
        val today = LocalDate.now()
        val todayHijri = HijrahDate.from(today)
        val out = mutableListOf<Event>()

        // Search a window that covers next year too.
        var search = today
        val end = today.plusDays(withinDays.toLong())
        val seen = mutableSetOf<String>()

        while (search.isBefore(end)) {
            val hijri = HijrahDate.from(search)
            val hMonth = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val hDay = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

            FIXED_EVENTS.forEach { (m, d, name) ->
                if (hMonth == m && hDay == d) {
                    val key = "$name-${hijri.get(java.time.temporal.ChronoField.YEAR)}"
                    if (key !in seen) {
                        seen.add(key)
                        out.add(
                            Event(
                                name = name,
                                gregorianDate = search,
                                hijriLabel = "$hDay/${hMonth}/${hijri.get(java.time.temporal.ChronoField.YEAR)}",
                                daysAway = ChronoUnit.DAYS.between(today, search),
                                kind = kindFor(m, d),
                                note = noteFor(m, d)
                            )
                        )
                    }
                }
            }

            // White days for each month
            if (hDay == 13) {
                val key = "white-${hijri.get(java.time.temporal.ChronoField.YEAR)}-$hMonth"
                if (key !in seen) {
                    seen.add(key)
                    out.add(
                        Event(
                            name = "White days begin (13th, 14th, 15th)",
                            gregorianDate = search,
                            hijriLabel = "$hDay/$hMonth/${hijri.get(java.time.temporal.ChronoField.YEAR)}",
                            daysAway = ChronoUnit.DAYS.between(today, search),
                            kind = "voluntary",
                            note = "Fasting on these three days each Hijri month is a recommended sunnah."
                        )
                    )
                }
            }

            search = search.plusDays(1)
        }

        return out.sortedBy { it.daysAway }
    }

    fun todayHijriLabel(): String {
        val h = HijrahDate.from(LocalDate.now())
        val d = h.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        val m = h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val y = h.get(java.time.temporal.ChronoField.YEAR)
        val monthName = HIJRI_MONTHS.getOrNull(m - 1) ?: ""
        return "$d $monthName $y"
    }

    private val HIJRI_MONTHS = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private fun kindFor(m: Int, d: Int): String = when {
        m == 9 -> "ramadan"
        m == 10 && d == 1 -> "eid"
        m == 12 && d == 10 -> "eid"
        m == 1 && d == 10 -> "fast"
        m == 12 && d == 9 -> "fast"
        else -> "significance"
    }

    private fun noteFor(m: Int, d: Int): String = when {
        m == 9 && d == 1 -> "The month of fasting. Begin with intention. Allah says: \"The month of Ramadan in which the Quran was revealed.\" (Qur'an 2:185)"
        m == 10 && d == 1 -> "Eid al-Fitr. The day of reward after the month of fasting."
        m == 12 && d == 10 -> "Eid al-Adha. The day of sacrifice and celebration."
        m == 1 && d == 10 -> "Ashura. Fasting on this day expiates the sins of the past year. (Sahih Muslim 1162)"
        m == 12 && d == 9 -> "Day of Arafah. Fasting on this day expiates the sins of the past year and the year to come. (Sahih Muslim 1162)"
        m == 9 && d == 27 -> "The Prophet  encouraged seeking Laylat al-Qadr on all odd nights of the last ten of Ramadan, not only the 27th."
        m == 3 && d == 12 -> "Some Muslims commemorate the Prophet's birthday today. Scholars differ on whether this is a valid practice. No definitive ruling is offered here."
        else -> ""
    }
}
