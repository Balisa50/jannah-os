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

// Clean white card with a soft shadow. No glassmorphism — light mode done well.
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = JannahSpacing.cardRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = shape, ambientColor = JannahColors.Border, spotColor = JannahColors.Border)
            .clip(shape)
            .background(JannahColors.Surface, shape)
            .border(width = 0.5.dp, color = JannahColors.Border, shape = shape)
    ) { content() }
}

// Screen scaffold — cream background, no glow.
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
