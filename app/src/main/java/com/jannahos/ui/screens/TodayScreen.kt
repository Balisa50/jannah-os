package com.jannahos.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.HijriDate
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.theme.BeadsGlyph
import com.jannahos.ui.theme.BookGlyph
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
    val history = WakeLog(ctx).lastNights(7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Assalamu Alaikum,",
                    style = JannahType.eyebrow, color = JannahColors.Orange)
                Spacer(Modifier.height(4.dp))
                Text(greeting,
                    style = JannahType.title, color = JannahColors.Ink)
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(JannahColors.Surface)
                    .clickable { onNavigate("you") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Alerts",
                    tint = JannahColors.Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        val greg = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date())
        val hijri = HijriDate.today()
        Text(
            if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
            style = JannahType.caption,
            color = JannahColors.Muted
        )

        Spacer(Modifier.height(JannahSpacing.l))

        // Hero — next prayer. No decorations.
        GlassCard {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("NEXT PRAYER",
                    style = JannahType.eyebrow, color = JannahColors.Orange)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    nextKey?.let { SalahTracker.label(it) } ?: "—",
                    style = JannahType.title,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    countdown,
                    style = JannahType.display,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.height(4.dp))
                Text("time left",
                    style = JannahType.caption, color = JannahColors.Muted)

                Spacer(Modifier.height(JannahSpacing.l))

                Box(
                    Modifier
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Orange)
                        .clickable { onNavigate("pray") }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("View prayer times  →",
                        style = JannahType.button,
                        color = JannahColors.OnOrange)
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // Tonight's plan
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
                        style = JannahType.eyebrow, color = JannahColors.Orange)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        when (level) {
                            1 -> "Level 1 · One night a week"
                            2 -> "Level 2 · Two nights a week"
                            3 -> "Level 3 · Three nights a week"
                            else -> "Level 4 · Every night"
                        },
                        style = JannahType.body, color = JannahColors.Ink
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("$prayedWeek of $target this week",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
                Text("→", style = JannahType.body, color = JannahColors.Muted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            QuickAction(
                modifier = Modifier.weight(1f),
                label = "Quran",
                sublabel = "Continue reading",
                glyph = { c -> BookGlyph(c, size = 24.dp) },
                onClick = { onNavigate("quran") }
            )
            QuickAction(
                modifier = Modifier.weight(1f),
                label = "Dhikr",
                sublabel = "Daily remembrance",
                glyph = { c -> BeadsGlyph(c, size = 24.dp) },
                onClick = { onNavigate("dhikr") }
            )
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // Weekly consistency
        GlassCard {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("WEEKLY CONSISTENCY",
                    style = JannahType.eyebrow, color = JannahColors.Orange)
                Spacer(Modifier.height(JannahSpacing.s))
                Text("$prayedWeek of 7 days",
                    style = JannahType.body, color = JannahColors.Ink)
                Spacer(Modifier.height(JannahSpacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)
                ) {
                    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
                    val prayedDays = history.filter { it.outcome == "prayed" }
                        .map { dayFmt.format(Date(it.ts)) }.toSet()
                    val last7 = (0..6).map {
                        dayFmt.format(Date(System.currentTimeMillis() - it * 24L * 60 * 60 * 1000))
                    }.reversed()
                    last7.forEach { d ->
                        val filled = prayedDays.contains(d)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) JannahColors.Orange
                                    else JannahColors.Border
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
    glyph: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
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
                glyph(JannahColors.Orange)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(label, style = JannahType.body, color = JannahColors.Ink,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(sublabel, style = JannahType.caption, color = JannahColors.Muted)
            }
        }
    }
}
