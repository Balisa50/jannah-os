package com.jannahos.ui.theme

import androidx.compose.ui.graphics.Color

object JannahColors {

    // Sand/cream canvas, deep warm ink, UniDash palette
    val Cream     = Color(0xFFEFE6D6)
    val Card      = Color(0xFFF8F2E8)
    val Surface   = Color(0xFFFBF7EF)
    val SurfaceAlt = Color(0xFFF2E8D6)

    // Burnt orange accent (UniDash)
    val Orange = Color(0xFFDD6B00)
    val OrangeSoft = Color(0xFFFFF3E0)
    val OrangeDeep = Color(0xFFB34F00)

    // Warm deep brown ink (not cold navy)
    val Ink    = Color(0xFF2B1410)
    val Body   = Color(0xFF3D2A20)
    val Muted  = Color(0xFF7A6656)
    val Faint  = Color(0xFFA89885)

    val Border  = Color(0xFFDDD0BA)
    val Divider = Color(0xFFE6DBC5)

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
