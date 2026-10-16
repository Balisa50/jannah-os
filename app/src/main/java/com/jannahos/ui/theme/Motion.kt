package com.jannahos.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

// A subtle fade-in-and-up entrance. Delay lets screens stagger their content.
// No bouncing — the app is quiet.
@Composable
fun RevealIn(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    distance: Float = 24f,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "revealAlpha"
    )
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else distance,
        animationSpec = tween(500),
        label = "revealOffset"
    )
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offset
        }
    ) { content() }
}

// A gentle celebratory pulse. One full cycle over 1.8 seconds.
@Composable
fun CelebrationPulse(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "celebrate")
    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) { content() }
}
