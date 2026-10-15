package com.jannahos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// A translucent glass surface. Subtle gradient fill + gradient edge highlight.
// This reads as real glass on OLED without expensive runtime blur.
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = JannahSpacing.cardRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(JannahBrushes.glassSurface, shape)
            .border(width = 0.6.dp, brush = JannahBrushes.glassEdge, shape = shape)
    ) { content() }
}

// Atmospheric light behind hero content. Wraps the whole screen.
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JannahColors.Deep)
    ) {
        // Soft radial gold glow at top — atmosphere, not decoration.
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxSize(0.6f)
                .background(JannahBrushes.goldGlow)
        )
        content()
    }
}
