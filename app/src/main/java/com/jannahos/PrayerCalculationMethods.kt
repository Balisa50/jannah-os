package com.jannahos

import com.batoulapps.adhan.CalculationMethod

/**
 * Converts the user's persisted prayer calculation preference into the
 * corresponding Adhan library parameters. Unknown/legacy values safely
 * fall back to Muslim World League rather than silently changing settings.
 */
object PrayerCalculationMethods {
    fun fromPreference(value: String?): CalculationMethod {
        return when (value?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_')) {
            "MWL", "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "EGYPT", "EGYPTIAN" -> CalculationMethod.EGYPTIAN
            "KARACHI", "UNIVERSITY_OF_ISLAMIC_SCIENCES_KARACHI" -> CalculationMethod.KARACHI
            "UMM_AL_QURA", "MAKKAH" -> CalculationMethod.UMM_AL_QURA
            "DUBAI" -> CalculationMethod.DUBAI
            "MOON_SIGHTING_COMMITTEE", "MOONSIGHTING_COMMITTEE", "MSC" -> CalculationMethod.MOONSIGHTING_COMMITTEE
            "NORTH_AMERICA", "ISNA" -> CalculationMethod.NORTH_AMERICA
            "KUWAIT" -> CalculationMethod.KUWAIT
            "QATAR" -> CalculationMethod.QATAR
            "SINGAPORE", "JAKIM" -> CalculationMethod.SINGAPORE
            "OTHER" -> CalculationMethod.OTHER
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }
}
