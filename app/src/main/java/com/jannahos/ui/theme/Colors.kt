package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

object JannahColors {

    // Canvas — warm cream, NOT white. This is the single most important value.
    val Cream      = Color(0xFFF7F4EF)
    val Card       = Color(0xFFFFFDFC)   // warm white, sits on cream
    val Surface    = Color(0xFFFFFFFF)   // pure white, used sparingly
    val SurfaceAlt = Color(0xFFFDF8F0)

    // Accent
    val Orange     = Color(0xFFF97316)
    val OrangeSoft = Color(0xFFFFE9D1)
    val OrangeDeep = Color(0xFFB45309)

    // Ink — warm near-black, not cold navy
    val Ink    = Color(0xFF141414)   // headings
    val Body   = Color(0xFF262626)   // body
    val Muted  = Color(0xFF7A7A7A)   // secondary
    val Faint  = Color(0xFFA0A0A0)

    // Hairlines
    val Border  = Color(0xFFE5DED6)  // warm greige
    val Divider = Color(0xFFEDE8E0)

    val Success = Color(0xFF1F7A5A)
    val Danger  = Color(0xFFB85454)
    val OnOrange = Color(0xFFFFFFFF)

    // Aliases for unmigrated screens
    val Void = Cream; val Deep = Cream; val Elevated = Card
    val Ivory = Ink; val Gold = Orange; val GoldDeep = OrangeDeep; val GoldInk = OnOrange
    val Emerald = Success; val Crimson = Danger; val Accent = Orange; val AccentInk = OnOrange
    val DayBg = Cream; val DaySurface = Card; val DayText = Ink; val DayMuted = Muted
    val NightBg = Cream; val NightSurface = Card; val NightText = Ink; val NightMuted = Muted
    val GlassTop = Color(0x14FFFFFF); val GlassBottom = Color(0x0AFFFFFF)
    val GlassEdgeTop = Color(0x1A000000); val GlassEdgeBot = Color(0x0A000000)
}
