package com.jannahos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val scheme = darkColorScheme(
    background = JannahColors.Deep,
    surface = JannahColors.Surface,
    onBackground = JannahColors.Ivory,
    onSurface = JannahColors.Ivory,
    primary = JannahColors.Gold,
    onPrimary = JannahColors.GoldInk,
    outline = JannahColors.Divider
)

@Composable
fun JannahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
