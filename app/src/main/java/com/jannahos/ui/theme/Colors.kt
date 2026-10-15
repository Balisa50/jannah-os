package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

// Single source of truth. Screens must not declare their own colours.
// Two palettes: Day (default screens) and Night (full-screen focus moments).
object JannahColors {

    // Day palette — used across the app's normal screens.
    val DayBg      = Color(0xFF0E1116)
    val DaySurface = Color(0xFF161A22)
    val DayText    = Color(0xFFE8E5DC)
    val DayMuted   = Color(0xFF8B8F98)

    // Night palette — reserved for focus moments: wake, urge, prayer prompt.
    // Deeper black so the screen recedes into the room.
    val NightBg      = Color(0xFF07090D)
    val NightSurface = Color(0xFF11141A)
    val NightText    = Color(0xFFE8E5DC)
    val NightMuted   = Color(0xFF8B8F98)

    // One accent. Used sparingly for the primary action on a screen.
    val Accent     = Color(0xFFC9A961)
    val AccentInk  = Color(0xFF0E1116) // Text colour on Accent buttons.

    // Semantic, thin.
    val Divider = Color(0xFF23272F)
    val Danger  = Color(0xFFB85454)
}
