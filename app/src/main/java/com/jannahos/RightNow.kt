package com.jannahos

import java.util.Calendar

object RightNow {
    fun getMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> "Right now, the Ummah in Jakarta is rising for Fajr."
            in 5..8 -> "Right now, the Ummah in Makkah is praying Fajr."
            in 9..11 -> "Right now, the Ummah in Cairo is starting their day with Dhuhr approaching."
            in 12..14 -> "Right now, the Ummah in London is praying Dhuhr."
            in 15..17 -> "Right now, the Ummah in Istanbul is preparing for Asr."
            in 18..19 -> "Right now, the Ummah in Dubai is breaking their fast or praying Maghrib."
            in 20..22 -> "Right now, the Ummah in New York is praying Isha."
            else -> "Right now, the Ummah in Malaysia is entering the night with Isha."
        }
    }
}
