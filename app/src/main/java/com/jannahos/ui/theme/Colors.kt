package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

object JannahColors {

    // Light surfaces
    val Cream     = Color(0xFFFFF9F2)
    val Surface   = Color(0xFFFFFFFF)
    val SurfaceTint = Color(0xFFFFF1E4)

    // Accent
    val Orange    = Color(0xFFFF6A00)
    val OrangeSoft = Color(0xFFFFE4CE)
    val OrangeDeep = Color(0xFFC45200)

    // Text
    val Ink       = Color(0xFF06143B) // headings
    val Body      = Color(0xFF3D4657)
    val Muted     = Color(0xFF8A92A2)
    val Faint     = Color(0xFFBFC5D0)

    // Lines
    val Border    = Color(0xFFEFE8DE)
    val Divider   = Color(0xFFF4EEE4)

    // Semantic
    val Success   = Color(0xFF2E7D5B)
    val Danger    = Color(0xFFB85454)

    // On-accent text
    val OnOrange  = Color(0xFFFFFFFF)

    // Legacy aliases used by screens we haven't migrated yet.
    val Void      = Cream
    val Deep      = Cream
    val Elevated  = SurfaceTint
    val Ivory     = Ink
    val Gold      = Orange
    val GoldDeep  = OrangeDeep
    val GoldInk   = OnOrange
    val Emerald   = Success
    val Crimson   = Danger
    val Accent    = Orange
    val AccentInk = OnOrange
    val DayBg     = Cream
    val DaySurface = Surface
    val DayText   = Ink
    val DayMuted  = Muted
    val NightBg      = Cream
    val NightSurface = Surface
    val NightText    = Ink
    val NightMuted   = Muted
    val GlassTop     = Color(0x14FFFFFF)
    val GlassBottom  = Color(0x0AFFFFFF)
    val GlassEdgeTop = Color(0x1A000000)
    val GlassEdgeBot = Color(0x0A000000)
}
