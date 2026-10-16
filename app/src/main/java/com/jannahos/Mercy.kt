package com.jannahos

object Mercy {

    data class Reminder(val text: String, val source: String)

    val list = listOf(
        Reminder(
            "\"Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins. Indeed, it is He who is the Forgiving, the Merciful.\"",
            "Qur'an 39:53"
        ),
        Reminder(
            "\"And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.\"",
            "Qur'an 2:186"
        ),
        Reminder(
            "\"Indeed, Allah loves those who repent and loves those who purify themselves.\"",
            "Qur'an 2:222"
        ),
        Reminder(
            "The Prophet ﷺ said: \"Allah is more joyful at the repentance of His servant than one of you who finds his lost camel in the desert.\"",
            "Sahih Muslim 2747"
        ),
        Reminder(
            "The Prophet ﷺ said: \"Every son of Adam sins, and the best of those who sin are those who repent.\"",
            "Sunan at-Tirmidhi 2499 (graded hasan)"
        ),
        Reminder(
            "The Prophet ﷺ said: \"Allah extends His hand at night so that the one who sinned during the day may repent, and He extends His hand during the day so that the one who sinned at night may repent.\"",
            "Sahih Muslim 2759"
        )
    )

    fun random(): Reminder = list.random()
}
