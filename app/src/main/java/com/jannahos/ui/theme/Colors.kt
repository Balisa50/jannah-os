package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

// A darker, richer palette. Layered surfaces for real depth.
// Gold is a gradient (see Brushes.kt), never a flat colour.
object JannahColors {

    // Backgrounds, deepest to lightest — used as a stack.
    val Void      = Color(0xFF05070A) // behind everything
    val Deep      = Color(0xFF0A0D13) // app background
    val Surface   = Color(0xFF11151D) // raised surface
    val Elevated  = Color(0xFF181D27) // most raised

    // Text.
    val Ivory     = Color(0xFFF2ECDF)
    val Body      = Color(0xFFC7C2B6)
    val Muted     = Color(0xFF7A7E88)
    val Faint     = Color(0xFF4A4E58)

    // Accent — champagne gold. Flat value, gradient in Brushes.kt.
    val Gold      = Color(0xFFD4B677)
    val GoldDeep  = Color(0xFF9E7F42)
    val GoldInk   = Color(0xFF0A0D13) // text on gold

    // Rare, restrained accents.
    val Emerald   = Color(0xFF5C9C7A)
    val Crimson   = Color(0xFFA85C5C)

    // Glass elements — drawn with alpha on top of surfaces.
    val GlassTop     = Color(0x0FFFFFFF) // 6% white
    val GlassBottom  = Color(0x05FFFFFF) // 2% white
    val GlassEdgeTop = Color(0x24FFFFFF) // 14% white
    val GlassEdgeBot = Color(0x08FFFFFF) // 3% white

    val Divider   = Color(0x14FFFFFF)

    // --- Backwards-compatible aliases ---
    // Screens still use these names; they map to the new tokens.
    val DayBg      = Deep
    val DaySurface = Surface
    val DayText    = Ivory
    val DayMuted   = Muted
    val NightBg      = Void
    val NightSurface = Surface
    val NightText    = Ivory
    val NightMuted   = Muted
    val Accent     = Gold
    val AccentInk  = GoldInk
    val Danger     = Crimson
}
