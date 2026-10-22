package com.jannahos

import java.util.Calendar

object FridayMode {

    fun isTodayFriday(): Boolean =
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

    data class Reminder(val title: String, val body: String, val source: String)

    fun reminders(): List<Reminder> = listOf(
        Reminder(
            title = "Surah Al-Kahf",
            body = "Recite Surah Al-Kahf today. The Prophet  said: \"Whoever recites Surah Al-Kahf on Friday, a light will shine for him between the two Fridays.\"",
            source = "Al-Hakim, graded sahih by al-Albani"
        ),
        Reminder(
            title = "Abundant salawat",
            body = "Send salawat on the Prophet . He said: \"Whoever sends one salah upon me, Allah sends ten upon him.\"",
            source = "Sahih Muslim 408"
        ),
        Reminder(
            title = "The hour of response",
            body = "There is an hour on Friday during which no Muslim asks Allah for something good except that He gives it. The last hour before Maghrib is a strong candidate.",
            source = "Sahih al-Bukhari 935, Sahih Muslim 852"
        ),
        Reminder(
            title = "Ghusl and perfume",
            body = "The Prophet  encouraged bathing, wearing clean clothes, and using perfume for Friday.",
            source = "Sahih al-Bukhari 883"
        )
    )
}
