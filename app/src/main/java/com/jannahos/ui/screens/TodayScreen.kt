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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val prayerTimes = SalahScheduler.todayTimes(lat, lon)
    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { prayerTimes[it] }?.time

    val countdown = if (nextTime != null) {
        val diff = nextTime - now
        String.format(Locale.US, "%02d:%02d:%02d",
            diff / 3600000, (diff / 60000) % 60, (diff / 1000) % 60)
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
    ) {
        // ═══════════════════════════════════════════
        // HERO ZONE — owns the top third of the screen.
        // Warm gradient, big type, no card.
        // ═══════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            JannahColors.OrangeSoft,
                            JannahColors.Cream
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(top = JannahSpacing.xxl)
            ) {
                Text(
                    "Assalamu Alaikum,",
                    style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep
                )
                Spacer(Modifier.height(JannahSpacing.s))
                Text(greeting, style = JannahType.title, color = JannahColors.Ink)

                Spacer(Modifier.height(JannahSpacing.s))

                val greg = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
                val hijri = HijriDate.today()
                Text(
                    if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
                    style = JannahType.caption,
                    color = JannahColors.Muted
                )

                Spacer(Modifier.height(JannahSpacing.l))

                // The main event — next prayer, huge.
                Text(
                    nextKey?.let { SalahTracker.label(it).uppercase() } ?: "—",
                    style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    countdown,
                    style = JannahType.display.copy(fontSize = 46.sp, lineHeight = 50.sp),
                    color = JannahColors.Ink
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    "remaining until ${nextKey?.let { SalahTracker.label(it) } ?: "prayer"}",
                    style = JannahType.caption,
                    color = JannahColors.Muted
                )
            }
        }

        // ═══════════════════════════════════════════
        // CONTENT ZONE — everything below the hero.
        // NOT cards. Section headers + content.
        // ═══════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            // ── Section: Tonight's plan ──
            Spacer(Modifier.height(JannahSpacing.l))
            SectionHeader("TONIGHT'S PLAN")
            Spacer(Modifier.height(JannahSpacing.s))

            // One card only — this is the one actionable unit.
            RoundedBox(onClick = { onNavigate("you") }) {
                Row(
                    Modifier.fillMaxWidth().padding(JannahSpacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            when (level) {
                                1 -> "Level 1 · One night a week"
                                2 -> "Level 2 · Two nights a week"
                                3 -> "Level 3 · Three nights a week"
                                else -> "Level 4 · Every night"
                            },
                            style = JannahType.body, color = JannahColors.Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("$prayedWeek of $target this week",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.body, color = JannahColors.Muted)
                }
            }

            // ── Section: This week ──
            Spacer(Modifier.height(JannahSpacing.xl))
            SectionHeader("THIS WEEK")
            Spacer(Modifier.height(JannahSpacing.s))

            // Weekly dots — no card. Sits directly on cream.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
                val prayedDays = history.filter { it.outcome == "prayed" }
                    .map { dayFmt.format(Date(it.ts)) }.toSet()
                val last7 = (0..6).map {
                    dayFmt.format(Date(System.currentTimeMillis() - it * 24L * 60 * 60 * 1000))
                }.reversed()
                last7.forEach { d ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (prayedDays.contains(d)) JannahColors.Orange
                                    else JannahColors.Border
                                )
                        )
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text(
                            d.take(1),
                            style = JannahType.micro,
                            color = JannahColors.Faint
                        )
                    }
                }
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Text("$prayedWeek of 7 days prayed",
                style = JannahType.caption, color = JannahColors.Muted)

            // ── Section: Today's prayers ──
            Spacer(Modifier.height(JannahSpacing.xl))
            SectionHeader("TODAY'S PRAYERS")
            Spacer(Modifier.height(JannahSpacing.s))

            // Prayer list — bare rows on cream. No card.
            val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
            val doneMap = SalahTracker.today(ctx)
            ordered.forEach { key ->
                val t = prayerTimes[key]
                val done = doneMap[key] == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("pray") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (done) JannahColors.Orange else JannahColors.Border)
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            SalahTracker.label(key),
                            style = JannahType.body,
                            color = JannahColors.Ink
                        )
                    }
                    Text(
                        t?.let { hhmm.format(it) } ?: "—",
                        style = JannahType.bodySmall,
                        color = if (done) JannahColors.Orange else JannahColors.Muted
                    )
                }
                if (key != "isha") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(JannahColors.Divider)
                    )
                }
            }

            // ── Section: Quick access ──
            Spacer(Modifier.height(JannahSpacing.xl))
            SectionHeader("QUICK ACCESS")
            Spacer(Modifier.height(JannahSpacing.s))

            // Two bare rows. Not cards.
            BareRow("Open Quran", "Continue reading") { onNavigate("quran") }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(JannahColors.Divider))
            BareRow("Dhikr", "Daily remembrance") { onNavigate("dhikr") }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(JannahColors.Divider))
            BareRow("Settings", "Location & calculation") { onNavigate("you") }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = JannahType.eyebrow,
        color = JannahColors.OrangeDeep,
        modifier = Modifier.fillMaxWidth()
    )
}

// The ONE card pattern. Warm white on cream, hairline border, no shadow.
@Composable
private fun RoundedBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .clickable(onClick = onClick)
    ) { content() }
}

@Composable
private fun BareRow(title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = JannahType.body, color = JannahColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(sub, style = JannahType.caption, color = JannahColors.Muted)
        }
        Text("→", style = JannahType.body, color = JannahColors.Muted)
    }
}
