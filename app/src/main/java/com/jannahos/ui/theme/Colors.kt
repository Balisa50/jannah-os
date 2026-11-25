package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

object JannahColors {

    // Sand/cream canvas, deep warm ink, UniDash palette
    val Cream     = Color(0xFFFFFFFF)
    val Card      = Color(0xFFF8F7F5)
    val Surface   = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF3F0ED)

    // Burnt orange accent (UniDash)
    val Orange = Color(0xFFC65A24)
    val OrangeSoft = Color(0xFFFFF1E8)
    val OrangeDeep = Color(0xFF9A3F1C)

    // Warm deep brown ink (not cold navy)
    val Ink    = Color(0xFF25211F)
    val Body   = Color(0xFF3B3531)
    val Muted  = Color(0xFF79716C)
    val Faint  = Color(0xFFAAA19A)

    val Border  = Color(0xFFE5DFD9)
    val Divider = Color(0xFFEEE9E5)

    val Success = Color(0xFF3E6B47)
    val Danger  = Color(0xFFA83A2E)
    val OnOrange = Color(0xFFFBF7EF)

    // Legacy aliases
    val Void = Cream; val Deep = Cream; val Elevated = Card
    val Ivory = Ink; val Gold = Orange; val GoldDeep = OrangeDeep; val GoldInk = OnOrange
    val Emerald = Success; val Crimson = Danger; val Accent = Orange; val AccentInk = OnOrange
    val DayBg = Cream; val DaySurface = Card; val DayText = Ink; val DayMuted = Muted
    val NightBg = Cream; val NightSurface = Card; val NightText = Ink; val NightMuted = Muted
    val GlassTop = Color(0x14FFFFFF); val GlassBottom = Color(0x0AFFFFFF)
    val GlassEdgeTop = Color(0x1A000000); val GlassEdgeBot = Color(0x0A000000)
}
