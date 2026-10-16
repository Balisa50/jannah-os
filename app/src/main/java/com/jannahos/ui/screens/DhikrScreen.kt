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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Adhkar
import com.jannahos.DhikrStore
import com.jannahos.ShakeDetector
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun DhikrScreen() {
    var selectedId by remember { mutableStateOf<String?>(null) }
    if (selectedId == null) DhikrList(onPick = { selectedId = it })
    else DhikrDetail(item = Adhkar.items.first { it.id == selectedId }, onBack = { selectedId = null })
}

@Composable
private fun DhikrList(onPick: (String) -> Unit) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("DHIKR",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Remember ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("Allah.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Adhkar.items.forEachIndexed { i, item ->
                val today = DhikrStore.countToday(ctx, item.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(item.id) }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.transliteration,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(item.translation,
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text(
                        "$today",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (today > 0) JannahColors.Orange else JannahColors.Faint
                    )
                    if (item.target != null) {
                        Spacer(Modifier.width(2.dp))
                        Text("/ ${item.target}", style = JannahType.micro, color = JannahColors.Faint)
                    }
                }
                if (i < Adhkar.items.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}

@Composable
private fun DhikrDetail(item: Adhkar.Item, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var count by remember { mutableStateOf(DhikrStore.countToday(ctx, item.id)) }
    var shakeEnabled by remember { mutableStateOf(true) }

    DisposableEffect(shakeEnabled) {
        val detector = if (shakeEnabled) ShakeDetector(ctx) {
            count = DhikrStore.increment(ctx, item.id)
        } else null
        detector?.start()
        onDispose { detector?.stop() }
    }

    val ringProgress = if (item.target != null)
        (count.toFloat() / item.target.toFloat()).coerceIn(0f, 1f) else 0f
    val ringAnim by animateFloatAsState(ringProgress, tween(400), label = "ring")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("← Back", style = JannahType.bodySmall, color = JannahColors.Muted,
            modifier = Modifier.clickable(onClick = onBack))

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(item.arabic, style = JannahType.arabicLarge,
                    color = JannahColors.Ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(item.transliteration, style = JannahType.bodySmall,
                    color = JannahColors.Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(item.translation, style = JannahType.body,
                    color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(item.source, style = JannahType.caption, color = JannahColors.Faint)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val stroke = 3f
                drawArc(
                    color = JannahColors.Border,
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (item.target != null) {
                    drawArc(
                        color = JannahColors.Orange,
                        startAngle = -90f, sweepAngle = 360f * ringAnim, useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = Size(size.width - stroke * 2, size.height - stroke * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", color = JannahColors.Ink,
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (item.target != null) "of ${item.target}" else "recitations",
                    style = JannahType.caption, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    if (shakeEnabled) "SHAKE OR TAP" else "TAP TO RECITE",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep
                )
            }
            Box(
                Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .clickable { count = DhikrStore.increment(ctx, item.id) }
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .clickable { shakeEnabled = !shakeEnabled }
                .padding(JannahSpacing.m)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Shake to count",
                        fontFamily = FontFamily.Serif, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(2.dp))
                    Text("Shake the phone to add one.",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (shakeEnabled) JannahColors.Orange else JannahColors.Border)
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .clickable { DhikrStore.reset(ctx, item.id); count = 0 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Reset today", style = JannahType.caption, color = JannahColors.Muted)
        }

        Spacer(Modifier.height(JannahSpacing.xl))
    }
}
