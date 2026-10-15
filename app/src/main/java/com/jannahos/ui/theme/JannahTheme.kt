package com.jannahos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The app is dark-only by design: worship moments are often at night,
// and a dark surface keeps focus on the text.
// Two schemes: Day (default) and Night (focus moments).

private val DayScheme = darkColorScheme(
    background   = JannahColors.DayBg,
    surface      = JannahColors.DaySurface,
    onBackground = JannahColors.DayText,
    onSurface    = JannahColors.DayText,
    primary      = JannahColors.Accent,
    onPrimary    = JannahColors.AccentInk,
    outline      = JannahColors.Divider
)

private val NightScheme = darkColorScheme(
    background   = JannahColors.NightBg,
    surface      = JannahColors.NightSurface,
    onBackground = JannahColors.NightText,
    onSurface    = JannahColors.NightText,
    primary      = JannahColors.Accent,
    onPrimary    = JannahColors.AccentInk,
    outline      = JannahColors.Divider
)

@Composable
fun JannahTheme(
    night: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (night) NightScheme else DayScheme,
        content = content
    )
}
