package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

object JannahColors {

    // Backgrounds — deeper cream so white cards actually stand out
    val Cream     = Color(0xFFF8EEDC)
    val Surface   = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFFDF8F0)

    // Accent
    val Orange     = Color(0xFFF97316)
    val OrangeSoft = Color(0xFFFFE9D1)
    val OrangeDeep = Color(0xFFB45309)

    // Text — proper contrast
    val Ink    = Color(0xFF0B1428)   // headings, nearly black
    val Body   = Color(0xFF2C3A4F)   // body text, dark slate
    val Muted  = Color(0xFF6E7686)   // captions, still readable
    val Faint  = Color(0xFFA0A8B8)

    // Lines
    val Border  = Color(0xFFE8DCC8)
    val Divider = Color(0xFFF0E6D2)

    // Semantic
    val Success = Color(0xFF2E7D5B)
    val Danger  = Color(0xFFB85454)

    // On-accent
    val OnOrange = Color(0xFFFFFFFF)

    // Legacy aliases
    val Void      = Cream
    val Deep      = Cream
    val Elevated  = SurfaceAlt
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
