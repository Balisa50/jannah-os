package com.jannahos.siraj.core

/**
 * A deliberately small, deterministic command parser.
 *
 * It does not pretend to understand arbitrary religious questions. It maps
 * clear navigation requests to a finite set of app-owned actions. Anything
 * outside that allowlist is returned as unsupported instead of guessed.
 */
object SirajCommandParser {
    fun parse(
        rawInput: String,
        surahs: List<Pair<Int, String>>
    ): SirajCommandResult {
        val input = normalize(rawInput)
        if (input.isBlank()) {
            return SirajCommandResult("Tell me what you want to open or find.")
        }

        val requestedAyah = resolveAyah(input, surahs)
        if (requestedAyah != null) {
            return SirajCommandResult(
                "Opening " + requestedAyah.third + ", ayah " + requestedAyah.second + ".",
                SirajAction.ShowAyah(
                    surah = requestedAyah.first,
                    ayah = requestedAyah.second,
                    surahName = requestedAyah.third
                )
            )
        }

        val surah = resolveSurah(input, surahs)
        if (surah != null) {
            return SirajCommandResult(
                "Opening Surah ${surah.second}.",
                SirajAction.OpenSurah(surah.first, surah.second)
            )
        }

        return when {
            containsAny(input, "open quran", "read quran", "show quran") ->
                SirajCommandResult("Opening the Quran.", SirajAction.OpenQuran)

            containsAny(input, "prayer times", "prayer time", "salah times", "show prayers", "open prayer") ->
                SirajCommandResult("Here are the prayer times for your saved location.", SirajAction.OpenPrayerTimes)

            containsAny(input, "qibla", "direction of qibla", "find qibla") ->
                SirajCommandResult("Opening the Qibla direction tool.", SirajAction.OpenQibla)

            containsAny(input, "search quran", "find in quran", "quran search") ->
                SirajCommandResult("Quran search is ready. Enter the word or phrase you want to find.", SirajAction.OpenQuranSearch)

            containsAny(input, "adhkar", "morning remembrance", "evening remembrance") ->
                SirajCommandResult("Opening your adhkar collection.", SirajAction.OpenAdhkar)

            containsAny(input, "my notes", "open notes", "notes") ->
                SirajCommandResult("Opening your private notes.", SirajAction.OpenNotes)

            containsAny(input, "settings", "app settings", "open settings") ->
                SirajCommandResult("Opening SIRAJ settings.", SirajAction.OpenSettings)

            containsAny(input, "help", "what can you do", "what can siraj do") ->
                SirajCommandResult(
                    "I can open a Surah by name or number, show prayer times, open Qibla, start Quran search, and take you to adhkar, notes or settings. I won't invent religious answers or claim a source I have not checked."
                )

            else -> SirajCommandResult(
                "I can't answer that reliably yet. I can navigate to your Quran, prayer times, Qibla, adhkar, notes and settings. For Islamic questions, SIRAJ must verify its sources before answering."
            )
        }
    }

    private fun resolveSurah(
        input: String,
        surahs: List<Pair<Int, String>>
    ): Pair<Int, String>? {
        val asksForSurah = input.contains("surah") ||
            input.startsWith("open ") ||
            input.startsWith("read ") ||
            input.startsWith("go to ")

        if (!asksForSurah) return null

        val number = Regex("""(?:surah|chapter)\s+(\d{1,3})""")
            .find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (number != null) return surahs.firstOrNull { it.first == number }

        val candidate = input
            .replace("open", "")
            .replace("read", "")
            .replace("go to", "")
            .replace("surah", "")
            .replace("chapter", "")
            .replace("please", "")
            .replace("the", "")
            .filter(Char::isLetterOrDigit)

        if (candidate.isBlank()) return null

        return surahs.firstOrNull { (_, name) ->
            val catalogName = name.lowercase().filter(Char::isLetterOrDigit)
            candidate == catalogName ||
                candidate.removePrefix("al") == catalogName.removePrefix("al")
        }
    }


    private fun resolveAyah(
        input: String,
        surahs: List<Pair<Int, String>>
    ): Triple<Int, Int, String>? {
        val numeric = Regex("""(?:surah\s+)?(\d{1,3})[:/](\d{1,3})""")
            .find(input)
        if (numeric != null) {
            val surahNumber = numeric.groupValues[1].toIntOrNull() ?: return null
            val ayahNumber = numeric.groupValues[2].toIntOrNull() ?: return null
            val surah = surahs.firstOrNull { it.first == surahNumber } ?: return null
            if (ayahNumber < 1) return null
            return Triple(surah.first, ayahNumber, surah.second)
        }

        val named = Regex("""(?:(?:open|read|show|find)\s+)?(?:surah\s+)?(.+?)\s+(?:ayah|verse)\s+(\d{1,3})""")
            .matchEntire(input) ?: return null
        val requestedName = named.groupValues[1]
            .trim()
            .filter(Char::isLetterOrDigit)
        val ayahNumber = named.groupValues[2].toIntOrNull() ?: return null
        if (requestedName.isBlank() || ayahNumber < 1) return null

        val surah = surahs.firstOrNull { (_, name) ->
            val catalogName = name.lowercase().filter(Char::isLetterOrDigit)
            requestedName == catalogName ||
                requestedName.removePrefix("al") == catalogName.removePrefix("al")
        } ?: return null

        return Triple(surah.first, ayahNumber, surah.second)
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun containsAny(value: String, vararg phrases: String): Boolean =
        phrases.any(value::contains)
}
