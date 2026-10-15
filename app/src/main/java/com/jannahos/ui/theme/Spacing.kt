package com.jannahos.ui.theme

import androidx.compose.ui.unit.dp

// A single spacing scale. No screen should invent its own value.
// If a size is not here, use the nearest one.
object JannahSpacing {
    val xs   = 4.dp
    val s    = 8.dp
    val m    = 16.dp
    val l    = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp

    // Screen gutters and default content width.
    val screenGutter = 24.dp
    val blockGap     = 32.dp
}
