package com.jannahos.siraj.core

import com.jannahos.siraj.actions.SirajDeviceAction

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

        val surah = resolveSurah(input, surahs)
        if (surah != null) {
            return SirajCommandResult(
                "Opening Surah ${surah.second}.",
                SirajAction.OpenSurah(surah.first, surah.second)
            )
        }

        return when {
            containsAny(input, "enable prayer lock", "turn on prayer lock") ->
                SirajCommandResult(
                    "I can enable Prayer Lock. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetPrayerLock(true),
                        "Enable Prayer Lock on this device?"
                    )
                )

            containsAny(input, "disable prayer lock", "turn off prayer lock") ->
                SirajCommandResult(
                    "I can disable Prayer Lock. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetPrayerLock(false),
                        "Disable Prayer Lock on this device?"
                    )
                )

            containsAny(input, "enable quran lock", "turn on quran lock", "enable quran reading lock") ->
                SirajCommandResult(
                    "I can enable the Quran reading reminder. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetQuranLock(true),
                        "Enable the Quran reading reminder?"
                    )
                )

            containsAny(input, "disable quran lock", "turn off quran lock", "disable quran reading lock") ->
                SirajCommandResult(
                    "I can disable the Quran reading reminder. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetQuranLock(false),
                        "Disable the Quran reading reminder?"
                    )
                )

            containsAny(input, "enable bedtime lock", "turn on bedtime lock") ->
                SirajCommandResult(
                    "I can enable the bedtime lock. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetBedtimeLock(true),
                        "Enable the bedtime lock?"
                    )
                )

            containsAny(input, "disable bedtime lock", "turn off bedtime lock") ->
                SirajCommandResult(
                    "I can disable the bedtime lock. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetBedtimeLock(false),
                        "Disable the bedtime lock?"
                    )
                )

            containsAny(input, "enable do not disturb", "turn on do not disturb") ->
                SirajCommandResult(
                    "I can turn on Do Not Disturb. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetDoNotDisturb(true),
                        "Turn on Do Not Disturb?"
                    )
                )

            containsAny(input, "disable do not disturb", "turn off do not disturb") ->
                SirajCommandResult(
                    "I can turn off Do Not Disturb. Confirm before I change this setting.",
                    SirajAction.RequestDeviceAction(
                        SirajDeviceAction.SetDoNotDisturb(false),
                        "Turn off Do Not Disturb?"
                    )
                )

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

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun containsAny(value: String, vararg phrases: String): Boolean =
        phrases.any(value::contains)
}
