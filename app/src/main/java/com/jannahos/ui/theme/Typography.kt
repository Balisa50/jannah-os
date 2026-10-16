package com.jannahos.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object JannahType {

    // Serif display, matching UniDash's editorial feel.
    val display = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.8).sp,
        lineHeight = 50.sp
    )

    val title = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
        lineHeight = 34.sp
    )

    // Italic serif, for emphasised words, matches UniDash's accent phrasing.
    val titleItalic = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        letterSpacing = (-0.3).sp,
        lineHeight = 34.sp
    )

    val eyebrow = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.2.sp
    )

    val body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 23.sp
    )
    val bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 19.sp
    )
    val caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
    val micro = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp
    )
    val button = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.2.sp
    )

    val arabic = TextStyle(
        fontSize = 28.sp,
        lineHeight = 52.sp,
        textAlign = TextAlign.Center
    )
    val arabicLarge = TextStyle(
        fontSize = 34.sp,
        lineHeight = 64.sp,
        textAlign = TextAlign.Center
    )

    val screenTitle = title; val section = eyebrow; val primary = body
    val secondary = bodySmall; val tertiary = caption
}
