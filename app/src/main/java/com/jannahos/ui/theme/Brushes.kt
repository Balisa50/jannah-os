package com.jannahos.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object JannahBrushes {
    // A warm cream wash behind the hero. Very subtle.
    val creamWash = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFF4E6), JannahColors.Cream)
    )

    // Orange gradient — for the one hero pill only.
    val orangeButton = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF8033), JannahColors.Orange)
    )

    // Alias used by screens we haven't migrated yet.
    val goldButton = orangeButton

    // Card edge — hairline at top, lighter at bottom.
    val cardEdge = Brush.verticalGradient(
        colors = listOf(Color(0x0F000000), Color(0x00000000))
    )

    // Fade for image overlays.
    val imageScrim = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0xCC06143B))
    )
}
