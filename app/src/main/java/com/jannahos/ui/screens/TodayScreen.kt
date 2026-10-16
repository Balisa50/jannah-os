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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val today = SalahTracker.today(ctx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Orange top banner with greeting
        Box(
            Modifier
                .fillMaxWidth()
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xxl, bottom = JannahSpacing.xl)
        ) {
            Column {
                Text(
                    "ASSALAMU ALAIKUM",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text(greeting + ", ", style = JannahType.title, color = JannahColors.OnOrange)
                    Text("Abdoulie.", style = JannahType.titleItalic, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.xs))
                val greg = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
                val hijri = HijriDate.today()
                Text(
                    if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.xl))

            // Next prayer hero — serif italic + big number
            Text("Next prayer", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.xs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    nextKey?.let { SalahTracker.label(it) } ?: "—",
                    style = JannahType.display,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "in",
                    style = JannahType.title.copy(fontStyle = FontStyle.Italic),
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(Modifier.height(JannahSpacing.xs))
            Text(
                countdown,
                color = JannahColors.Ink,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp,
                lineHeight = 60.sp
            )

            Spacer(Modifier.height(JannahSpacing.l))

            // Pill CTA
            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable { onNavigate("pray") }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    "View all prayers  →",
                    style = JannahType.button,
                    color = JannahColors.OnOrange
                )
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Tonight's plan card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable { onNavigate("you") }
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("TONIGHT'S PLAN",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when (level) {
                                1 -> "One night "
                                2 -> "Two nights "
                                3 -> "Three nights "
                                else -> "Every night "
                            },
                            style = JannahType.title.copy(fontSize = 22.sp),
                            color = JannahColors.Ink
                        )
                        Text(
                            "a week.",
                            style = JannahType.titleItalic.copy(fontSize = 22.sp),
                            color = JannahColors.Orange
                        )
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    Text("$prayedWeek of $target this week",
                        style = JannahType.bodySmall,
                        color = JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            // Today's prayers — bare list with dividers
            Text("TODAY'S PRAYERS",
                style = JannahType.eyebrow,
                color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
            ordered.forEachIndexed { i, key ->
                val t = prayerTimes[key]
                val done = today[key] == true
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("pray") }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (done) JannahColors.Orange else JannahColors.Border)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            SalahTracker.label(key),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink
                        )
                    }
                    Text(
                        t?.let { hhmm.format(it) } ?: "—",
                        style = JannahType.body,
                        color = if (done) JannahColors.Orange else JannahColors.Muted
                    )
                }
                if (i < ordered.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Week dots
            Text("THIS WEEK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
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
                        Text(d.take(1), style = JannahType.micro, color = JannahColors.Faint)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Quick access — two pills
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                QuickPill(
                    modifier = Modifier.weight(1f),
                    label = "Quran",
                    sub = "Continue reading",
                    onClick = { onNavigate("quran") }
                )
                QuickPill(
                    modifier = Modifier.weight(1f),
                    label = "Dhikr",
                    sub = "Remember Allah",
                    onClick = { onNavigate("dhikr") }
                )
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun QuickPill(
    modifier: Modifier = Modifier,
    label: String,
    sub: String,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .clickable(onClick = onClick)
            .padding(JannahSpacing.l)
    ) {
        Column {
            Text(
                label,
                style = JannahType.title.copy(fontSize = 20.sp),
                color = JannahColors.Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(sub, style = JannahType.caption, color = JannahColors.Muted)
        }
    }
}
