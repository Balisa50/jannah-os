package com.jannahos

// Every act here is verifiable from an authentic source. The reward is
// described exactly as the source states it — no amplification, no invention.
// The point is not points. The point is showing the user, honestly, that a
// spare two minutes is not empty.
object DeedsEngine {

    data class Act(
        val minutes: Int,
        val title: String,
        val reward: String,
        val source: String,
        val tier: String   // "instant", "short", "sit-down"
    )

    // Sort by duration. The engine picks based on the user's free window.
    val acts: List<Act> = listOf(
        // ---- Instant, under 1 minute ----
        Act(1, "Say Astaghfirullah 3×",
            "\"Whoever says Astaghfirullaha wa atubu ilayh, his sins are forgiven even if he fled from battle.\"",
            "Sunan Abi Dawud 1517 (graded hasan)",
            "instant"),
        Act(1, "Say SubhanAllahi wa bihamdihi 1×",
            "\"Whoever says it once, his sins fall away from him even if they are like the foam of the sea.\"",
            "Sahih al-Bukhari 6405, Sahih Muslim 2691",
            "instant"),
        Act(1, "Say La ilaha illallah wahdahu... 10×",
            "Whoever says it ten times in a day, it is as if he freed four slaves from the children of Isma'il.",
            "Sahih al-Bukhari 6404",
            "instant"),
        Act(1, "Send salawat on the Prophet ﷺ 1×",
            "\"Whoever sends one salah upon me, Allah sends ten upon him.\"",
            "Sahih Muslim 408",
            "instant"),
        Act(1, "Say SubhanAllah, Alhamdulillah, Allahu Akbar 33× each",
            "The Prophet ﷺ said this expiates sins even if they are like the foam of the sea.",
            "Sahih Muslim 597",
            "instant"),

        // ---- Short, 2-5 minutes ----
        Act(3, "Give a small sadaqah",
            "\"Charity does not decrease wealth.\"",
            "Sahih Muslim 2588",
            "short"),
        Act(3, "Call a parent",
            "\"A man asked the Prophet ﷺ who deserves his best companionship. He said: Your mother, then your mother, then your mother, then your father.\"",
            "Sahih al-Bukhari 5971",
            "short"),
        Act(4, "Read Ayat al-Kursi",
            "\"Whoever reads it when going to bed, a protector from Allah remains with him, and Shaytan does not come near.\"",
            "Sahih al-Bukhari 5010",
            "short"),
        Act(5, "Read Surah Al-Ikhlas 3×",
            "The Prophet ﷺ said it equals a third of the Quran.",
            "Sahih al-Bukhari 5013",
            "short"),
        Act(5, "Make dua for a fellow Muslim",
            "\"The dua of a Muslim for his brother in his absence is answered.\"",
            "Sahih Muslim 2733",
            "short"),
        Act(5, "Smile at someone, help with a small task",
            "\"Your smile in the face of your brother is charity.\"",
            "Jami' at-Tirmidhi 1956 (graded hasan)",
            "short"),

        // ---- Sit-down, 10-15 minutes ----
        Act(10, "Read Surah Al-Mulk before sleep",
            "\"Whoever recites it every night, Allah will protect him from the punishment of the grave.\"",
            "Jami' at-Tirmidhi 2891 (hasan by some, da'if by others)",
            "sit-down"),
        Act(10, "Read Surah Al-Kahf (Friday)",
            "\"Whoever recites Surah Al-Kahf on Friday, a light shines for him between the two Fridays.\"",
            "Al-Hakim, graded sahih by al-Albani",
            "sit-down"),
        Act(12, "Pray 2 rak'ah of Duha",
            "\"Whoever prays Duha, his sins are forgiven even if they are like the foam of the sea.\"",
            "Jami' at-Tirmidhi 476 (graded hasan)",
            "sit-down"),
        Act(15, "Pray 2 rak'ah of tahajjud tonight",
            "The Prophet ﷺ said: the last third of the night, Allah descends and asks: who supplicates, that I may answer?",
            "Sahih al-Bukhari 1145, Sahih Muslim 758",
            "sit-down")
    )

    // Picks the best act for a given free window.
    // If they have 1 minute, offers an instant one. If 15, offers a sit-down.
    fun suggestFor(minutes: Int, seed: Int = 0): Act {
        val fits = acts.filter { it.minutes <= minutes }
        if (fits.isEmpty()) return acts.first()
        return fits[seed % fits.size]
    }

    fun tierLabel(act: Act): String = when (act.tier) {
        "instant" -> "Under a minute"
        "short" -> "A few minutes"
        else -> "Sit down"
    }
}
