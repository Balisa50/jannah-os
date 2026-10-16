package com.jannahos.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

// The most cinematic screen in the app. Reserved for the moment the tahajjud
// alarm fires. Slow reveal, breathing crescent, one pulsing action.
@Composable
fun WakeScreenCinematic(
    onAwake: () -> Unit,
    onSnooze: () -> Unit,
    onNotNow: () -> Unit
) {
    // Slow reveal sequence
    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(80); stage = 1     // crescent fades in
        delay(400); stage = 2     // "It is time" reveals
        delay(500); stage = 3     // body text
        delay(700); stage = 4     // button appears
    }

    // Letter spacing animation on the title
    val titleSpacing by animateFloatAsState(
        targetValue = if (stage >= 2) 4f else 14f,
        animationSpec = tween(1400),
        label = "titleSpacing"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = tween(900),
        label = "titleAlpha"
    )
    val crescentAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(2000),
        label = "crescent"
    )
    val crescentScale by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0.85f,
        animationSpec = tween(2400),
        label = "crescentScale"
    )
    val bodyAlpha by animateFloatAsState(
        targetValue = if (stage >= 3) 1f else 0f,
        animationSpec = tween(1000),
        label = "bodyAlpha"
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (stage >= 4) 1f else 0f,
        animationSpec = tween(700),
        label = "buttonAlpha"
    )

    // Breathing pulse on the awake button
    val pulse by rememberInfiniteTransition(label = "wakePulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Background: deep ink with a slow orange glow at the bottom
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF05070A),
                        Color(0xFF0E0810),
                        Color(0xFF160A08)
                    )
                )
            )
    ) {
        // Warm glow rising from the bottom
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            JannahColors.Orange.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Crescent — slow breathing, soft orange
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        this.alpha = crescentAlpha
                        scaleX = crescentScale * pulse
                        scaleY = crescentScale * pulse
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension * 0.32f

                    // Outer glow
                    drawCircle(
                        color = JannahColors.Orange.copy(alpha = 0.06f),
                        radius = r * 2.2f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = JannahColors.Orange.copy(alpha = 0.10f),
                        radius = r * 1.6f,
                        center = Offset(cx, cy)
                    )

                    // Crescent body
                    drawCircle(
                        color = JannahColors.Orange.copy(alpha = 0.85f),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    // Cut-out to make the crescent
                    drawCircle(
                        color = Color(0xFF0E0810),
                        radius = r * 0.82f,
                        center = Offset(cx + r * 0.42f, cy - r * 0.15f)
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            // Title with animating letter spacing
            Text(
                "It is time.",
                fontFamily = FontFamily.Serif,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                color = JannahColors.OnOrange,
                letterSpacing = titleSpacing.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha }
            )

            Spacer(Modifier.height(JannahSpacing.m))

            Text(
                "Get up. Make wudu. Pray.",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                color = JannahColors.OnOrange.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = bodyAlpha }
            )

            Spacer(Modifier.height(JannahSpacing.xxl))

            // Primary action — pulsing
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .graphicsLayer {
                        alpha = buttonAlpha
                        scaleX = if (stage >= 4) pulse else 1f
                        scaleY = if (stage >= 4) pulse else 1f
                    }
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF8A3D),
                                JannahColors.Orange
                            )
                        )
                    )
                    .clickable(onClick = onAwake),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "I am awake",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.OnOrange,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Secondary — softer, textual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = buttonAlpha * 0.85f }
            ) {
                Box(
                    Modifier
                        .clickable(onClick = onSnooze)
                        .padding(vertical = 8.dp, horizontal = 20.dp)
                ) {
                    Text(
                        "15 more minutes",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        color = JannahColors.OnOrange.copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .clickable(onClick = onNotNow)
                        .padding(vertical = 8.dp, horizontal = 20.dp)
                ) {
                    Text(
                        "Not now",
                        fontSize = 13.sp,
                        color = JannahColors.OnOrange.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}
