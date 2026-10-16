package com.jannahos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Duas
import com.jannahos.IstighfarStore
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun DhikrScreen() {
    val ctx = LocalContext.current
    var count by remember { mutableStateOf(IstighfarStore.todayCount(ctx)) }
    val dua = Duas.istighfar

    // A subtle ring: fills proportionally toward 100 recitations. Not a score.
    val progress by animateFloatAsState(
        (count.coerceAtMost(100) / 100f),
        tween(500),
        label = "ring"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Dhikr", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Today · $count", style = JannahType.caption, color = JannahColors.Muted)

        Spacer(Modifier.height(JannahSpacing.l))

        // Arabic is the hero.
        GlassCard {
            Column(
                Modifier.padding(JannahSpacing.l).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dua.arabic, style = JannahType.arabicLarge, color = JannahColors.Ivory,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(dua.transliteration, style = JannahType.bodySmall,
                    color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(dua.translation, style = JannahType.body,
                    color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 24.sp)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(dua.source, style = JannahType.caption, color = JannahColors.Faint)
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Counter with ring — tap to increment.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val stroke = 3f
                drawArc(
                    color = JannahColors.Divider,
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = JannahColors.Gold,
                    startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    count.toString(),
                    style = JannahType.display, color = JannahColors.Ivory
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("TAP TO RECITE",
                    style = JannahType.eyebrow, color = JannahColors.Muted)
            }

            // Invisible tap target over the whole ring.
            Box(
                Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(110.dp))
                    .clickable { count = IstighfarStore.increment(ctx) }
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text(
            "The Prophet ﷺ sought forgiveness more than seventy times a day.",
            style = JannahType.caption, color = JannahColors.Faint,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JannahSpacing.s))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.buttonRadius))
                .clickable {
                    IstighfarStore.reset(ctx)
                    count = 0
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Reset today", style = JannahType.caption, color = JannahColors.Muted)
        }

        Spacer(Modifier.height(JannahSpacing.xl))
    }
}
