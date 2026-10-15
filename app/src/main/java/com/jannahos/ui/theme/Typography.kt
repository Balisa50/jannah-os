package com.jannahos.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// One scale, editorial in feel. Serif for large display; sans for body.
object JannahType {

    // Very large, editorial. Used for hero numbers (e.g. countdown).
    val display = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 44.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.5).sp,
        lineHeight = 52.sp
    )

    // Page title. Serif — gives warmth.
    val title = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 34.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.3).sp,
        lineHeight = 42.sp
    )

    // Section header — small caps feel, wide tracking.
    val eyebrow = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp
    )

    // Primary content.
    val body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    )

    val bodySmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )

    val caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )

    val micro = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )

    // Button label.
    val button = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    )

    // Arabic. Generous line height for diacritics.
    val arabic = TextStyle(
        fontSize = 26.sp,
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

    // --- Backwards-compatible aliases ---
    val screenTitle = title
    val section     = eyebrow
    val primary     = body
    val secondary   = bodySmall
    val tertiary    = caption
}
