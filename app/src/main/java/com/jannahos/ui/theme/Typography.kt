package com.jannahos.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// One type scale. Weight and size carry hierarchy; nothing is bold for decoration.
object JannahType {

    // Screen title — the one heading at the top of a full screen.
    val screenTitle = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 2.sp
    )

    // Section header — small, uppercase-feeling label above a block.
    val section = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )

    // Primary content — the value the user came to read.
    val primary = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal
    )

    // Secondary — labels, hints, timestamps.
    val secondary = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal
    )

    // Tertiary — the smallest supporting text; use sparingly.
    val tertiary = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    )

    // Button label — consistent across every button in the app.
    val button = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )

    // Arabic text — larger line height to give diacritics room.
    val arabic = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 44.sp
    )
}
