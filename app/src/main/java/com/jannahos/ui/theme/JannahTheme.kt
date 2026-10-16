package com.jannahos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val scheme = lightColorScheme(
    background = JannahColors.Cream,
    surface = JannahColors.Surface,
    onBackground = JannahColors.Ink,
    onSurface = JannahColors.Ink,
    primary = JannahColors.Orange,
    onPrimary = JannahColors.OnOrange,
    outline = JannahColors.Border
)

@Composable
fun JannahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
