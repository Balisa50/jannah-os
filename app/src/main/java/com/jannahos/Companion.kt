package com.jannahos

import java.util.Calendar

// Picks the most relevant contextual message right now, based on weather and time.
// Every message carries a verified source. When nothing applies, returns null.
object Companion {

    data class Message(
        val title: String,
        val body: String,
        val source: String,
        val kind: String
    )

    fun decide(weather: WeatherApi.Current?): Message? {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Priority 1: rain right now
        if (weather != null && WeatherApi.isRaining(weather.weatherCode)) {
            return Message(
                title = "It is raining",
                body = "اللَّهُمَّ صَيِّبًا نَافِعًا\n\nAllahumma sayyiban nafi'an\n\n\"O Allah, make it a beneficial rain.\"",
                source = "Sahih al-Bukhari 1032",
                kind = "rain"
            )
        }

        // Priority 2: very hot
        if (weather != null && WeatherApi.isHot(weather.temperatureC)) {
            return Message(
                title = "It is hot today",
                body = "Drink water in sips, not in one breath. The Prophet  said: \"None of you should drink in one breath; rather drink in two or three.\"",
                source = "Jami' at-Tirmidhi 1886 (graded sahih by al-Albani)",
                kind = "heat"
            )
        }

        // Priority 3: bedtime window
        if (hour in 21..22) {
            return Message(
                title = "Before sleep",
                body = "Recite Surah Al-Mulk tonight. The Prophet  said: \"Whoever recites Surah Al-Mulk every night, Allah will protect him from the punishment of the grave.\"",
                source = "Jami' at-Tirmidhi 2891 (hasan by some scholars, da'if by others)",
                kind = "bedtime"
            )
        }

        // Priority 4: pre-dawn window
        if (hour in 4..5) {
            return Message(
                title = "Before Fajr",
                body = "The last third of the night has passed or is passing. Allah says: \"And in the hours before dawn they would seek forgiveness.\"",
                source = "Qur'an 51:18",
                kind = "dawn"
            )
        }

        return null
    }
}
