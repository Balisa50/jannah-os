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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DailyHadith
import com.jannahos.FridayMode
import com.jannahos.HijriDate
import com.jannahos.RamadanMode
import com.jannahos.RightNow
import com.jannahos.SafeStore
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.TahajjudScheduler
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

    val today = SalahTracker.today(ctx)
    val markedCount = today.count { it.value }
    val pendingCount = 5 - markedCount

    val userName = Settings.displayName(ctx)

    // Right Now — one intelligent suggestion
    val prayerMs = prayerTimes.mapValues { it.value.time }
    val rightNow = remember(tick / 60) { RightNow.decide(ctx, prayerMs) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // ── Slim greeting header ──────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.l, bottom = JannahSpacing.m)
        ) {
            Text(
                if (userName.isBlank()) greeting + "."
                else greeting + ", " + userName + ".",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink
            )
            Spacer(Modifier.height(2.dp))
            val greg = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
            val hijri = HijriDate.today()
            Text(
                if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
                style = JannahType.caption,
                color = JannahColors.Muted
            )
        }

        // ── Ramadan / Friday banners, slim ────────────────────
        val ramadan = RamadanMode.current()
        if (ramadan.active || (ramadan.daysUntil in 1..7)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(if (ramadan.isLastTen) JannahColors.OrangeDeep else JannahColors.OrangeSoft)
                    .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)
            ) {
                Text(
                    when {
                        ramadan.active && ramadan.isLastTen -> "Last ten · night ${ramadan.dayOfRamadan}"
                        ramadan.active -> "Ramadan · day ${ramadan.dayOfRamadan}"
                        else -> "Ramadan in ${ramadan.daysUntil} days"
                    },
                    style = JannahType.eyebrow,
                    color = if (ramadan.isLastTen) JannahColors.OnOrange else JannahColors.OrangeDeep
                )
            }
        } else if (FridayMode.isTodayFriday()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(JannahColors.OrangeSoft)
                    .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)
            ) {
                Text("Friday · recite Al-Kahf today",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {

            // ── ONE Right Now card ────────────────────────────
            if (rightNow != null) {
                Spacer(Modifier.height(JannahSpacing.m))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeDeep)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("RIGHT NOW",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.7f))
                        Spacer(Modifier.height(6.dp))
                        Text(rightNow.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange,
                            lineHeight = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(rightNow.body,
                            style = JannahType.bodySmall,
                            color = JannahColors.OnOrange.copy(alpha = 0.85f),
                            lineHeight = 20.sp)
                        if (rightNow.actionLabel != null && rightNow.actionRoute != null) {
                            Spacer(Modifier.height(JannahSpacing.m))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(JannahColors.OnOrange)
                                    .clickable { onNavigate(rightNow.actionRoute) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(rightNow.actionLabel,
                                    style = JannahType.caption,
                                    color = JannahColors.OrangeDeep,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Next prayer countdown ─────────────────────────
            Spacer(Modifier.height(JannahSpacing.l))
            Text("NEXT PRAYER",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    nextKey?.let { SalahTracker.label(it) } ?: "—",
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.width(8.dp))
                Text("in",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(countdown,
                fontFamily = FontFamily.Serif,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                letterSpacing = (-1).sp,
                lineHeight = 46.sp)

            // ── 5-dot prayer status row ───────────────────────
            Spacer(Modifier.height(JannahSpacing.l))
            Text(
                if (pendingCount == 0) "All prayers marked · Alhamdulillah"
                else if (pendingCount == 1) "1 prayer unmarked"
                else "$pendingCount prayers unmarked",
                style = JannahType.bodySmall,
                color = if (pendingCount == 0) JannahColors.OrangeDeep else JannahColors.Muted
            )
            Spacer(Modifier.height(JannahSpacing.s))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable { onNavigate("pray") }
                    .padding(JannahSpacing.m),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ordered.forEach { key ->
                    val done = today[key] == true
                    val isNext = key == nextKey
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> JannahColors.Orange
                                        isNext -> JannahColors.OrangeDeep
                                        else -> JannahColors.Border
                                    }
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            SalahTracker.label(key).take(3),
                            style = JannahType.micro,
                            color = if (done) JannahColors.Ink else JannahColors.Muted
                        )
                    }
                }
            }

            // ── Tonight's plan (compact, honest) ──────────────
            val level = TahajjudPlan.level(ctx)
            val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
            Spacer(Modifier.height(JannahSpacing.m))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable { onNavigate("you") }
                    .padding(JannahSpacing.m)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TAHAJJUD",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (level) {
                                1 -> "One night a week"
                                2 -> "Two nights a week"
                                3 -> "Three nights a week"
                                else -> "Every night"
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("$prayedWeek of $target nights this week",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            // ── Quran + Dhikr, side by side ───────────────────
            Spacer(Modifier.height(JannahSpacing.m))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                TileCard(
                    Modifier.weight(1f),
                    eyebrow = "QURAN",
                    title = "Continue",
                    onClick = { onNavigate("quran") }
                )
                TileCard(
                    Modifier.weight(1f),
                    eyebrow = "DHIKR",
                    title = "Remember",
                    onClick = { onNavigate("dhikr") }
                )
            }

            // ── Hadith of the day ─────────────────────────────
            val h = DailyHadith.today()
            Spacer(Modifier.height(JannahSpacing.m))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("HADITH · ${h.theme.uppercase()}",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(h.english,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        color = JannahColors.Body,
                        lineHeight = 24.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(h.source,
                        style = JannahType.micro, color = JannahColors.Faint)
                }
            }

        }
    }
}

@Composable
private fun TileCard(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
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
            Text(eyebrow,
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(title,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
        }
    }
}
