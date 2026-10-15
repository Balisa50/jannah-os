package com.jannahos.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object JannahBrushes {

    // Soft atmospheric light behind hero content. Radial, fades to nothing.
    val goldGlow = Brush.radialGradient(
        colors = listOf(
            JannahColors.Gold.copy(alpha = 0.18f),
            JannahColors.Gold.copy(alpha = 0.05f),
            Color.Transparent
        ),
        center = Offset(0.5f, 0.3f),
        radius = 700f
    )

    // Frosted glass: subtle vertical gradient from top to bottom.
    val glassSurface = Brush.verticalGradient(
        colors = listOf(JannahColors.GlassTop, JannahColors.GlassBottom)
    )

    // The edge highlight that reads as "glass" — brighter at top.
    val glassEdge = Brush.verticalGradient(
        colors = listOf(JannahColors.GlassEdgeTop, JannahColors.GlassEdgeBot)
    )

    // Gold gradient for the primary action button — light → dark.
    val goldButton = Brush.verticalGradient(
        colors = listOf(Color(0xFFE5CB90), JannahColors.Gold, JannahColors.GoldDeep)
    )

    // Faint vertical fade under content — gives the composition a base.
    val surfaceFade = Brush.verticalGradient(
        colors = listOf(Color.Transparent, JannahColors.Void)
    )
}
