package com.jannahos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// White card, visible warm border, soft shadow. Reads as intentional, not washed.
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = JannahSpacing.cardRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = JannahColors.Border,
                spotColor = JannahColors.Border
            )
            .clip(shape)
            .background(JannahColors.Surface, shape)
            .border(width = 1.dp, color = JannahColors.Border, shape = shape)
    ) { content() }
}

@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) { content() }
}
