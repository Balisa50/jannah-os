package com.jannahos

import com.batoulapps.adhan.CalculationMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerCalculationMethodsTest {
    @Test
    fun defaultsToMuslimWorldLeagueForMissingOrUnknownPreference() {
        assertEquals(
            CalculationMethod.MUSLIM_WORLD_LEAGUE,
            PrayerCalculationMethods.fromPreference(null)
        )
        assertEquals(
            CalculationMethod.MUSLIM_WORLD_LEAGUE,
            PrayerCalculationMethods.fromPreference("not-a-method")
        )
    }

    @Test
    fun mapsSupportedMethodPreferences() {
        val cases = mapOf(
            "MWL" to CalculationMethod.MUSLIM_WORLD_LEAGUE,
            "EGYPT" to CalculationMethod.EGYPTIAN,
            "KARACHI" to CalculationMethod.KARACHI,
            "UMM_AL_QURA" to CalculationMethod.UMM_AL_QURA,
            "DUBAI" to CalculationMethod.DUBAI,
            "MOON_SIGHTING_COMMITTEE" to CalculationMethod.MOONSIGHTING_COMMITTEE,
            "NORTH_AMERICA" to CalculationMethod.NORTH_AMERICA,
            "KUWAIT" to CalculationMethod.KUWAIT,
            "QATAR" to CalculationMethod.QATAR,
            "SINGAPORE" to CalculationMethod.SINGAPORE,
            "OTHER" to CalculationMethod.OTHER
        )

        cases.forEach { (preference, expected) ->
            assertEquals(preference, expected, PrayerCalculationMethods.fromPreference(preference))
        }
    }

    @Test
    fun acceptsLegacyDisplayNamesAndWhitespace() {
        assertEquals(
            CalculationMethod.MUSLIM_WORLD_LEAGUE,
            PrayerCalculationMethods.fromPreference(" Muslim World League ")
        )
        assertEquals(
            CalculationMethod.UMM_AL_QURA,
            PrayerCalculationMethods.fromPreference("Umm al-Qura")
        )
    }
}
