package com.jannahos.ui.screens

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import com.jannahos.Companion
import com.jannahos.DiaryClassifier
import com.jannahos.DiaryStore
import com.jannahos.WeatherApi
import com.jannahos.HijriDate
import com.jannahos.RamadanMode
import com.jannahos.RightNow
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.RevealIn
import com.jannahos.ui.theme.CelebrationPulse
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

    var companionMsg by remember { mutableStateOf<Companion.Message?>(null) }
    val diaryLatest = remember { DiaryStore(ctx).latestTagIn24h() }
    val diaryReaction = remember {
        diaryLatest?.let { e ->
            val r = DiaryClassifier.classify(e.text)
            r.takeIf { it.reactionTitle.isNotBlank() }
        }
    }
    val prayerTimesMs = remember { prayerTimes.mapValues { it.value.time } }
    var rightNow by remember { mutableStateOf(RightNow.decide(ctx, prayerTimesMs)) }
    LaunchedEffect(Unit) {
        val w = withContext(Dispatchers.IO) { WeatherApi.fetch(lat, lon) }
        companionMsg = Companion.decide(w)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Ramadan context — active or within 7 days out
        val ramadan = RamadanMode.current()
        if (ramadan.active || (ramadan.daysUntil in 1..7)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (ramadan.isLastTen) JannahColors.OrangeDeep else JannahColors.Orange
                    )
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(vertical = JannahSpacing.m)
            ) {
                Column {
                    Text(
                        when {
                            ramadan.active && ramadan.isLastTen -> "THE LAST TEN · NIGHT ${ramadan.dayOfRamadan}"
                            ramadan.active -> "RAMADAN · DAY ${ramadan.dayOfRamadan}"
                            else -> "RAMADAN IN ${ramadan.daysUntil} DAYS"
                        },
                        style = JannahType.eyebrow,
                        color = JannahColors.OnOrange.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            ramadan.isOddNight -> "An odd night. Strive tonight."
                            ramadan.isLastTen -> "The last ten are here. Every night counts."
                            ramadan.active -> "Fasting, Quran, and dua. The month is moving."
                            else -> "Prepare. Fast, read, and set your intention."
                        },
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = JannahColors.OnOrange,
                        lineHeight = 24.sp
                    )
                    if (ramadan.active) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            RamadanMode.dayGuidance.suhoorNote,
                            style = JannahType.caption,
                            color = JannahColors.OnOrange.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // "Right Now" card — single intelligent suggestion
        if (rightNow != null) {
            val rn = rightNow!!
            // Log this suggestion shown once per screen compose
            androidx.compose.runtime.LaunchedEffect(rn.kind) {
                com.jannahos.RightNow.trackShown(ctx, rn.kind)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(JannahColors.OrangeDeep)
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(vertical = JannahSpacing.m)
            ) {
                Column {
                    Text("RIGHT NOW",
                        style = JannahType.eyebrow,
                        color = JannahColors.OnOrange.copy(alpha = 0.7f))
                    Spacer(Modifier.height(4.dp))
                    Text(rn.title,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = JannahColors.OnOrange,
                        lineHeight = 26.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(rn.body,
                        style = JannahType.bodySmall,
                        color = JannahColors.OnOrange.copy(alpha = 0.9f),
                        lineHeight = 20.sp)
                    if (rn.actionLabel != null && rn.actionRoute != null) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.OnOrange.copy(alpha = 0.15f))
                                .clickable {
                                    com.jannahos.RightNow.trackActed(ctx, rn.kind)
                                    onNavigate(rn.actionRoute)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(rn.actionLabel,
                                style = JannahType.caption,
                                color = JannahColors.OnOrange,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Diary celebration / reaction — shown for 24h after an entry
        if (diaryReaction != null) {
            val r = diaryReaction!!
            val isMilestone = r.tag.startsWith("milestone")
            val bannerContent: @androidx.compose.runtime.Composable () -> Unit = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (isMilestone) JannahColors.OrangeDeep else JannahColors.OrangeSoft
                    )
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(vertical = JannahSpacing.m)
            ) {
                Column {
                    Text(
                        if (isMilestone) "TODAY" else "YOU WROTE",
                        style = JannahType.eyebrow,
                        color = if (isMilestone) JannahColors.OnOrange.copy(alpha = 0.7f) else JannahColors.OrangeDeep
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(r.reactionTitle,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = if (isMilestone) JannahColors.OnOrange else JannahColors.Ink,
                        lineHeight = 26.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(r.reactionBody,
                        style = JannahType.bodySmall,
                        color = if (isMilestone) JannahColors.OnOrange.copy(alpha = 0.9f) else JannahColors.Body,
                        lineHeight = 20.sp)
                    if (r.source.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(r.source,
                            style = JannahType.micro,
                            color = if (isMilestone) JannahColors.OnOrange.copy(alpha = 0.7f) else JannahColors.Faint)
                    }
                    if (r.actionRoute != null) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(if (isMilestone) JannahColors.OnOrange.copy(alpha = 0.15f) else JannahColors.Ink)
                                .clickable { onNavigate(r.actionRoute) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                when (r.actionRoute) {
                                    "tawbah" -> "Open tawbah"
                                    "pray" -> "Open prayer"
                                    "dhikr" -> "Open dhikr"
                                    else -> "Open"
                                },
                                style = JannahType.caption,
                                color = if (isMilestone) JannahColors.OnOrange else JannahColors.OnOrange,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            }
            }
            if (isMilestone) {
                RevealIn(delayMillis = 0) { CelebrationPulse { bannerContent() } }
            } else {
                RevealIn(delayMillis = 0) { bannerContent() }
            }
        }

        // Orange top banner with greeting
        RevealIn(delayMillis = 60) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text(
                    "ASSALAMU ALAIKUM",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                val storedName = Settings.displayName(ctx)
                Text(
                    if (storedName.isBlank()) greeting + "."
                    else greeting + ", " + storedName + ".",
                    style = JannahType.title.copy(fontSize = 24.sp),
                    color = JannahColors.OnOrange
                )
                Spacer(Modifier.height(2.dp))
                val greg = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
                val hijri = HijriDate.today()
                Text(
                    if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
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

            // Companion card — only when there is something to say
            if (companionMsg != null) {
                val msg = companionMsg!!
                Spacer(Modifier.height(JannahSpacing.l))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(msg.title.uppercase(),
                            style = JannahType.eyebrow,
                            color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(msg.body,
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(msg.source,
                            style = JannahType.micro,
                            color = JannahColors.Faint)
                    }
                }
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
