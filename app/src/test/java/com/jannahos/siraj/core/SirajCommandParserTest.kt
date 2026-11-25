package com.jannahos.siraj.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajCommandParserTest {
    private val surahs = listOf(
        1 to "Al-Fatihah",
        18 to "Al-Kahf",
        36 to "Ya-Sin",
        67 to "Al-Mulk"
    )

    @Test
    fun opensSurahByNumber() {
        val result = SirajCommandParser.parse("Open Surah 18", surahs)
        assertEquals(SirajAction.OpenSurah(18, "Al-Kahf"), result.action)
        assertEquals("Opening Surah Al-Kahf.", result.reply)
    }

    @Test
    fun opensSurahByNameWithDifferentSpacingAndHyphens() {
        val result = SirajCommandParser.parse("read surah al kahf", surahs)
        assertEquals(SirajAction.OpenSurah(18, "Al-Kahf"), result.action)
    }

    @Test
    fun routesOnlyExplicitSupportedCommands() {
        assertEquals(
            SirajAction.OpenQuran,
            SirajCommandParser.parse("open Quran", surahs).action
        )
        assertEquals(
            SirajAction.OpenPrayerTimes,
            SirajCommandParser.parse("show prayer times", surahs).action
        )
        assertEquals(
            SirajAction.OpenQibla,
            SirajCommandParser.parse("find qibla", surahs).action
        )
        assertEquals(
            SirajAction.OpenQuranSearch,
            SirajCommandParser.parse("search Quran", surahs).action
        )
        assertEquals(
            SirajAction.OpenSettings,
            SirajCommandParser.parse("open settings", surahs).action
        )
    }

    @Test
    fun refusesToGuessUnsupportedReligiousQuestions() {
        val result = SirajCommandParser.parse(
            "Is this hadith authentic and what is its ruling?",
            surahs
        )
        assertEquals(null, result.action)
        assertTrue(result.reply.contains("verify its sources"))
    }

    @Test
    fun blankInputReturnsPrompt() {
        val result = SirajCommandParser.parse("   ", surahs)
        assertEquals(null, result.action)
        assertTrue(result.reply.isNotBlank())
    }

    @Test
    fun pageNumberIsNotMisreadAsASurahNumber() {
        val result = SirajCommandParser.parse("open Quran page 18", surahs)
        assertEquals(SirajAction.OpenQuran, result.action)
    }
}
