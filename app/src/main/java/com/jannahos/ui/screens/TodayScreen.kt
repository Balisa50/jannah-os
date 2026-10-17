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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.pressClickable
import com.jannahos.ui.revealOnLoad
import com.jannahos.DailyHadith
import com.jannahos.FridayMode
import com.jannahos.HijriDate
import com.jannahos.RamadanMode
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
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
    // Read tick so the composable recomposes each second.
    val ignore = tick
    var revealCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..6) { kotlinx.coroutines.delay(30); revealCount = i }
    }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val prayerTimes = remember(todayKey, lat, lon) { SalahScheduler.todayTimes(lat, lon) }
    val now = System.currentTimeMillis()

    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { prayerTimes[it] }?.time

    // If all prayers passed today, show tomorrow's Fajr
    val isAfterIsha = nextKey == null
    val displayLabel = if (isAfterIsha) "Fajr" else SalahTracker.label(nextKey!!)
    val displayTime = if (isAfterIsha) {
        // Rough estimate: tomorrow's Fajr is ~24h later
        (prayerTimes["fajr"]?.time ?: now) + 24L * 60 * 60 * 1000
    } else nextTime!!

    val diffMs = (displayTime - now).coerceAtLeast(0L)
    val countdown = String.format(Locale.US, "%02d:%02d:%02d",
        diffMs / 3600000, (diffMs / 60000) % 60, (diffMs / 1000) % 60)

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Peaceful night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }

    val userName = Settings.displayName(ctx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Greeting
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.l, bottom = JannahSpacing.m)
                .revealOnLoad(revealCount >= 1)
        ) {
            Text(
                if (userName.isBlank()) greeting + "." else "$greeting, $userName.",
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
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

        // Ramadan / Friday banner
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

            Spacer(Modifier.height(JannahSpacing.l))

            // Next prayer hero
            Text("NEXT PRAYER", modifier = Modifier.revealOnLoad(revealCount >= 3),
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.revealOnLoad(revealCount >= 3), verticalAlignment = Alignment.Bottom) {
                Text(displayLabel,
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink)
                Spacer(Modifier.width(8.dp))
                Text("in",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp,
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 5.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(countdown, modifier = Modifier.revealOnLoad(revealCount >= 3),
                fontFamily = FontFamily.Serif,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                letterSpacing = (-1).sp,
                lineHeight = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                if (isAfterIsha) "Tomorrow" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(displayTime)),
                style = JannahType.caption,
                color = JannahColors.Muted
            )

            Spacer(Modifier.height(JannahSpacing.l))

            // Tahajjud card — the only "tracking" in the app
            val level = TahajjudPlan.level(ctx)
            val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Orange)
                    .pressClickable { onNavigate("you") }
                    .revealOnLoad(revealCount >= 4)
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TAHAJJUD",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when (level) {
                                1 -> "One night a week"
                                2 -> "Two nights a week"
                                3 -> "Three nights a week"
                                else -> "Every night"
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("$prayedWeek of $target nights this week",
                            style = JannahType.caption,
                            color = JannahColors.OnOrange.copy(alpha = 0.85f))
                    }
                    Text("→",
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            // Quran
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .pressClickable { onNavigate("quran") }
                    .revealOnLoad(revealCount >= 5)
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("QURAN",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(6.dp))
                        Text("Continue reading",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            // Hadith of the day
            val h = DailyHadith.today()
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
                    .revealOnLoad(revealCount >= 6)
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
                    Text(h.source, style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
