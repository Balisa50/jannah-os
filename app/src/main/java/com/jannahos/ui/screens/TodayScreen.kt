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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FrictionMap
import com.jannahos.HijriDate
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.theme.BeadsGlyph
import com.jannahos.ui.theme.BookGlyph
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.MosqueBackdrop
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(onNavigate: (String) -> Unit = {}) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) { delay(1000); tick++ }
    }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val prayerTimes = SalahScheduler.todayTimes(lat, lon)
    val now = System.currentTimeMillis()

    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { prayerTimes[it] }?.time

    val countdown = if (nextTime != null) {
        val diff = nextTime - now
        val s = (diff / 1000) % 60
        val m = (diff / (1000 * 60)) % 60
        val h = diff / (1000 * 60 * 60)
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    } else "--:--:--"

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Peaceful night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)

    // Weekly dots — last 7 nights, filled if prayed
    val history = WakeLog(ctx).lastNights(7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text("Assalamu Alaikum,",
                    style = JannahType.caption, color = JannahColors.Muted)
                Spacer(Modifier.height(2.dp))
                Text(greeting,
                    style = JannahType.title, color = JannahColors.Ivory)
            }
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JannahColors.Surface)
                    .clickable { onNavigate("you") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = JannahColors.Ivory,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // Date line with Hijri
        val greg = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date())
        val hijri = HijriDate.today()
        Text(
            if (hijri.isNotEmpty()) "$greg  ·  $hijri" else greg,
            style = JannahType.caption,
            color = JannahColors.Muted
        )

        Spacer(Modifier.height(JannahSpacing.l))

        // --- Hero: Next prayer with mosque backdrop ---
        Box {
            GlassCard {
                Box(Modifier.fillMaxWidth()) {
                    // Backdrop silhouette, top-right of the card
                    Box(
                        Modifier
                            .fillMaxWidth(0.55f)
                            .height(150.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        MosqueBackdrop(Modifier.fillMaxSize())
                    }

                    Column(Modifier.padding(JannahSpacing.m)) {
                        Text("NEXT PRAYER",
                            style = JannahType.eyebrow, color = JannahColors.Muted)
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text(
                            nextKey?.let { SalahTracker.label(it) } ?: "—",
                            style = JannahType.body, color = JannahColors.Ivory,
                            fontSize = 18.sp, fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text(countdown,
                            style = JannahType.display, color = JannahColors.Ivory)
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text("Time left",
                            style = JannahType.caption, color = JannahColors.Muted)

                        Spacer(Modifier.height(JannahSpacing.l))

                        Box(
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(JannahColors.Gold)
                                .clickable { onNavigate("pray") }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("View prayer times  →",
                                style = JannahType.caption,
                                color = JannahColors.GoldInk,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // --- Tonight's plan ---
        GlassCard {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("you") }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("TONIGHT'S PLAN",
                        style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.xs))
                    Text(
                        when (level) {
                            1 -> "Level 1 · One night a week"
                            2 -> "Level 2 · Two nights a week"
                            3 -> "Level 3 · Three nights a week"
                            else -> "Level 4 · Every night"
                        },
                        style = JannahType.body, color = JannahColors.Ivory,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("$prayedWeek of $target this week",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
                Text("→", style = JannahType.body, color = JannahColors.Muted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // --- Quick actions: Quran + Dhikr side by side ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            QuickAction(
                modifier = Modifier.weight(1f),
                label = "Quran",
                sublabel = "Continue reading",
                glyph = { color -> BookGlyph(color, size = 22.dp) },
                onClick = { onNavigate("quran") }
            )
            QuickAction(
                modifier = Modifier.weight(1f),
                label = "Dhikr",
                sublabel = "Daily remembrance",
                glyph = { color -> BeadsGlyph(color, size = 22.dp) },
                onClick = { onNavigate("dhikr") }
            )
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // --- Weekly consistency ---
        GlassCard {
            Column(Modifier.padding(JannahSpacing.m)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("WEEKLY CONSISTENCY",
                            style = JannahType.eyebrow, color = JannahColors.Muted)
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text("$prayedWeek of 7 days",
                            style = JannahType.body, color = JannahColors.Ivory,
                            fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)
                ) {
                    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
                    val prayedDays = history.filter { it.outcome == "prayed" }.map { dayFmt.format(Date(it.ts)) }.toSet()
                    val last7 = (0..6).map {
                        dayFmt.format(Date(System.currentTimeMillis() - it * 24L * 60 * 60 * 1000))
                    }.reversed()
                    last7.forEach { d ->
                        val filled = prayedDays.contains(d)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) JannahColors.Gold else JannahColors.Faint.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    label: String,
    sublabel: String,
    glyph: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        GlassCard {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(JannahSpacing.m)
            ) {
                glyph(JannahColors.Gold)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(label, style = JannahType.body, color = JannahColors.Ivory,
                    fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(sublabel, style = JannahType.caption, color = JannahColors.Muted)
            }
        }
    }
}
