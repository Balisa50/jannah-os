package com.jannahos

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

// Detects proximity to Ramadan and the shape of the month.
object RamadanMode {

    data class Phase(
        val active: Boolean,
        val daysUntil: Long,
        val dayOfRamadan: Int,   // 0 if not yet in Ramadan
        val isLastTen: Boolean,
        val isOddNight: Boolean, // 21, 23, 25, 27, 29
        val hijriMonth: Int,
        val hijriDay: Int
    )

    fun current(): Phase {
        val today = LocalDate.now()
        val hijri = HijrahDate.from(today)
        val hm = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val hd = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

        // Are we in Ramadan now?
        val inRamadan = hm == 9

        // Days until next Ramadan (approximate)
        val daysUntil = if (inRamadan) 0L else {
            // Scan forward up to 400 days
            var d = today.plusDays(1)
            var count = 1L
            var found = false
            while (count < 400) {
                val h = HijrahDate.from(d)
                if (h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) == 9 &&
                    h.get(java.time.temporal.ChronoField.DAY_OF_MONTH) == 1) {
                    found = true
                    break
                }
                d = d.plusDays(1)
                count++
            }
            if (found) count else -1
        }

        val isLastTen = inRamadan && hd >= 21
        val isOddNight = inRamadan && hd in listOf(21, 23, 25, 27, 29)

        return Phase(
            active = inRamadan,
            daysUntil = daysUntil,
            dayOfRamadan = if (inRamadan) hd else 0,
            isLastTen = isLastTen,
            isOddNight = isOddNight,
            hijriMonth = hm,
            hijriDay = hd
        )
    }

    // Today's suhoor/iftar context, informational only.
    data class DayGuidance(val suhoorNote: String, val iftarDuaArabic: String, val iftarDuaTranslation: String, val iftarSource: String)

    val dayGuidance = DayGuidance(
        suhoorNote = "The Prophet  said: \"Eat suhoor, for in suhoor there is blessing.\" (Sahih al-Bukhari 1923)",
        iftarDuaArabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
        iftarDuaTranslation = "The thirst has gone, the veins are moistened, and the reward is certain if Allah wills.",
        iftarSource = "Sunan Abi Dawud 2357 (graded hasan)"
    )

    // The last ten nights, what to do each night
    data class NightFocus(val night: Int, val note: String)

    val lastTenFocus = listOf(
        NightFocus(21, "Possible Laylat al-Qadr. Pray, make dua, seek forgiveness."),
        NightFocus(22, "Keep going. The Prophet  would stay awake and wake his family."),
        NightFocus(23, "Possible Laylat al-Qadr. Make the dua the Prophet  taught Aisha."),
        NightFocus(24, "Rest, but do not stop. Every night of the ten is precious."),
        NightFocus(25, "Possible Laylat al-Qadr. Increase Quran and dua."),
        NightFocus(26, "The Prophet  said: seek it in all of the last ten."),
        NightFocus(27, "The most commonly held opinion. Strive tonight."),
        NightFocus(28, "Still the ten. Still the chance."),
        NightFocus(29, "Possible Laylat al-Qadr. The last odd night."),
        NightFocus(30, "The month closes. Thank Allah for reaching it.")
    )

    // The dua Aisha asked about, narrated in Tirmidhi
    val laylatulQadrDua = Triple(
        "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
        "Allahumma innaka 'afuwwun tuhibbu-l-'afwa fa-'fu 'anni",
        "O Allah, You are the Pardoner, You love to pardon. So pardon me."
    )

    const val laylatulQadrSource = "Jami' at-Tirmidhi 3513, Sunan Ibn Majah 3850 (graded sahih)"
}
