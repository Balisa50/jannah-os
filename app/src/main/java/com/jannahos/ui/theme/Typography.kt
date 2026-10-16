package com.jannahos.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object JannahType {

    val display = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1).sp,
        lineHeight = 46.sp
    )

    val title = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
        lineHeight = 34.sp
    )

    val eyebrow = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.8.sp
    )

    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp)
    val bodySmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    val micro = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)

    val button = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp)

    val arabic = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 52.sp,
        textAlign = TextAlign.Center
    )
    val arabicLarge = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 64.sp,
        textAlign = TextAlign.Center
    )

    // Aliases for unmigrated screens.
    val screenTitle = title
    val section     = eyebrow
    val primary     = body
    val secondary   = bodySmall
    val tertiary    = caption
}
