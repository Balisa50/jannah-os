package com.jannahos.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object JannahType {

    // Hero numbers — 42sp, bold. This is what your eye lands on.
    val display = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.4).sp,
        lineHeight = 44.sp
    )

    // Page title — 24sp, bold. Big enough to anchor the screen.
    val title = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        lineHeight = 30.sp
    )

    // Section headers — 11sp, wide tracking, uppercase feel. The label above a group.
    val eyebrow = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.6.sp
    )

    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
    val bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    val micro = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    val button = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp)

    val arabic = TextStyle(fontSize = 28.sp, lineHeight = 52.sp, textAlign = TextAlign.Center)
    val arabicLarge = TextStyle(fontSize = 34.sp, lineHeight = 64.sp, textAlign = TextAlign.Center)

    val screenTitle = title; val section = eyebrow; val primary = body
    val secondary = bodySmall; val tertiary = caption
}
