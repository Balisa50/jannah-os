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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FrictionMap
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.TahajjudScheduler
import com.jannahos.WakeLog
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen() {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }

    // Refresh every second so the countdown ticks live.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val nextTahajjud = TahajjudScheduler.computeTonight(lat, lon)

    // "Tonight" meaning: if it's after midnight and before Fajr, we're in tonight.
    val nowMs = System.currentTimeMillis()
    val targetMs = nextTahajjud.time
    val totalMs = (targetMs - nowMs).coerceAtLeast(0)
    val seconds = (totalMs / 1000) % 60
    val minutes = (totalMs / (1000 * 60)) % 60
    val hours = totalMs / (1000 * 60 * 60)
    val countdown = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val progress = ((24f * 60f * 60f - totalMs / 1000f) / (24f * 60f * 60f)).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(progress, tween(900), label = "ring")

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val onTonight = TahajjudPlan.isOnTonight(ctx)

    val insight = FrictionMap.topInsight(ctx)
    val prayed7 = FrictionMap.prayedLast7Days(ctx)
    val log = WakeLog(ctx)
    val history = log.lastNights(7)
    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

    // Greeting based on hour
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Late night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }

    val dateFmt = SimpleDateFormat("EEEE · d MMMM", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        // --- Greeting block ---
        Text(greeting, style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text(dateFmt.format(Date()), style = JannahType.caption, color = JannahColors.Muted)

        Spacer(Modifier.height(JannahSpacing.xl))

        // --- Hero countdown with progress ring ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val stroke = 3f
                // Track
                drawArc(
                    color = JannahColors.Divider,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    color = JannahColors.Gold,
                    startAngle = -90f,
                    sweepAngle = 360f * animProgress,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TAHAJJUD IN", style = JannahType.eyebrow, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(countdown, style = JannahType.display, color = JannahColors.Ivory)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(targetMs)),
                    style = JannahType.bodySmall,
                    color = JannahColors.Gold
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // --- Primary action ---
        GoldButton(
            label = "Set tahajjud alarm",
            onClick = { TahajjudScheduler.schedule(ctx, nextTahajjud); tick++ }
        )

        val scheduled = TahajjudScheduler.nextScheduled(ctx)
        Spacer(Modifier.height(JannahSpacing.s))
        Text(
            if (scheduled > 0L) "Alarm set · " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scheduled))
            else "No alarm set",
            style = JannahType.caption,
            color = JannahColors.Muted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(JannahSpacing.xl))

        // --- Formation card ---
        GlassCard {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("FORMATION", style = JannahType.eyebrow, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    when (level) {
                        1 -> "One night a week"
                        2 -> "Two nights a week"
                        3 -> "Three nights a week"
                        else -> "Every night"
                    },
                    style = JannahType.body, color = JannahColors.Ivory,
                    fontSize = 18.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    (if (onTonight) "Scheduled tonight · " else "Rest tonight · ") +
                        "$prayedWeek of $target this week",
                    style = JannahType.caption, color = JannahColors.Muted
                )
            }
        }

        // --- Pattern card ---
        if (prayed7 > 0 || insight != null) {
            Spacer(Modifier.height(JannahSpacing.s))
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("PATTERN", style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    if (prayed7 > 0) {
                        Text(
                            "$prayed7 of last 7 nights prayed",
                            style = JannahType.body, color = JannahColors.Ivory
                        )
                        Spacer(Modifier.height(JannahSpacing.xs))
                    }
                    if (insight != null) {
                        Text(
                            insight.suggestion,
                            style = JannahType.bodySmall, color = JannahColors.Gold,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // --- Recent nights card ---
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(JannahSpacing.s))
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("LAST 7 NIGHTS", style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    history.forEach { e ->
                        val day = dayFmt.format(Date(e.ts))
                        val status = when (e.outcome) {
                            "prayed" -> "Prayed"
                            "missed" -> "Missed"
                            else -> e.outcome
                        }
                        val sc = if (e.outcome == "prayed") JannahColors.Gold else JannahColors.Muted
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day, style = JannahType.bodySmall, color = JannahColors.Muted)
                            Text(status, style = JannahType.bodySmall, color = sc)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

@Composable
private fun GoldButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(JannahSpacing.buttonRadius))
            .background(com.jannahos.ui.theme.JannahBrushes.goldButton)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = JannahType.button, color = JannahColors.GoldInk)
    }
}
