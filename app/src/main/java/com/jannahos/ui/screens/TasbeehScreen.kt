package com.jannahos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ShakeDetector
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

// Post-salah tasbeeh: 33 SubhanAllah, 33 Alhamdulillah, 34 Allahu Akbar.
private data class Step(val label: String, val arabic: String, val translit: String, val target: Int)

private val STEPS = listOf(
    Step("SubhanAllah", "سُبْحَانَ اللَّهِ", "Glory be to Allah", 33),
    Step("Alhamdulillah", "الْحَمْدُ لِلَّهِ", "All praise is for Allah", 33),
    Step("Allahu Akbar", "اللَّهُ أَكْبَرُ", "Allah is the Greatest", 34)
)

@Composable
fun TasbeehScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var stepIndex by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }
    var shakeEnabled by remember { mutableStateOf(true) }

    DisposableEffect(shakeEnabled, stepIndex) {
        val detector = if (shakeEnabled && stepIndex < STEPS.size) ShakeDetector(ctx) {
            if (count < STEPS[stepIndex].target) {
                count++
                if (count >= STEPS[stepIndex].target) {
                    // Auto-advance after a short pause, just increment index
                    // next tap will start the new step.
                }
            }
        } else null
        detector?.start()
        onDispose { detector?.stop() }
    }

    val step = STEPS.getOrNull(stepIndex)
    val target = step?.target ?: 1
    val progress = (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val ringAnim by animateFloatAsState(progress, tween(300), label = "ring")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(JannahSpacing.xl))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("After salah", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Text("skip", style = JannahType.caption, color = JannahColors.Muted,
                modifier = Modifier.clickable { onDone() }.padding(4.dp))
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Progress steps
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            STEPS.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                i < stepIndex -> JannahColors.Orange
                                i == stepIndex -> JannahColors.OrangeDeep
                                else -> JannahColors.Border
                            }
                        )
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        if (step == null) {
            // Done
            Text("Alhamdulillah.",
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("The sunnah tasbeeh is complete.",
                style = JannahType.body, color = JannahColors.Body,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.xxl))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Orange)
                    .clickable { onDone() },
                contentAlignment = Alignment.Center
            ) {
                Text("Return", style = JannahType.button, color = JannahColors.OnOrange)
            }
            return@Column
        }

        // Arabic
        Text(step.arabic,
            fontSize = 42.sp,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif)
        Spacer(Modifier.height(JannahSpacing.s))
        Text(step.label,
            style = JannahType.body, color = JannahColors.Muted)
        Spacer(Modifier.height(JannahSpacing.s))
        Text(step.translit,
            style = JannahType.caption, color = JannahColors.Faint)

        Spacer(Modifier.height(JannahSpacing.xl))

        // Counter ring
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val stroke = 4f
                drawArc(
                    color = JannahColors.Border,
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = JannahColors.Orange,
                    startAngle = -90f, sweepAngle = 360f * ringAnim, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count",
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = (-2).sp,
                    lineHeight = 76.sp)
                Spacer(Modifier.height(4.dp))
                Text("of $target",
                    style = JannahType.caption, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    if (shakeEnabled) "SHAKE OR TAP" else "TAP TO COUNT",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep
                )
            }
            Box(
                Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (count < step.target) {
                            count++
                        }
                    }
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .clickable { shakeEnabled = !shakeEnabled },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (shakeEnabled) "Shake: on" else "Shake: off",
                    style = JannahType.button, color = JannahColors.Ink
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(
                        if (count >= step.target) JannahColors.Orange else JannahColors.Card
                    )
                    .clickable {
                        if (stepIndex < STEPS.size - 1) {
                            stepIndex++
                            count = 0
                        } else {
                            stepIndex++
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (stepIndex < STEPS.size - 1) "Next" else "Finish",
                    style = JannahType.button,
                    color = if (count >= step.target) JannahColors.OnOrange else JannahColors.Muted
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))
    }
}
