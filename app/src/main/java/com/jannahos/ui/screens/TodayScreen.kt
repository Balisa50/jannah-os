package com.jannahos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.*
import com.jannahos.ui.pressClickable
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
    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val prayerTimes = remember(todayKey, lat, lon) { SalahScheduler.todayTimes(lat, lon) }
    
    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { prayerTimes[it] }?.time

    val isAfterIsha = nextKey == null
    val displayLabel = if (isAfterIsha) "Fajr" else SalahTracker.label(nextKey!!)
    val displayTime = if (isAfterIsha) {
        (prayerTimes["fajr"]?.time ?: now) + 24L * 60 * 60 * 1000
    } else nextTime!!

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
        // HERO PHOTO BLOCK
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            Image(
                painter = painterResource(id = com.jannahos.R.drawable.hero_today),
                contentDescription = "Today",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dark gradient for white text readability
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(bottom = JannahSpacing.m)
            ) {
                Text(
                    if (userName.isBlank()) greeting + "." else "$greeting, $userName.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(2.dp))
                val greg = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
                val hijri = HijriDate.today()
                Text(
                    if (hijri.isNotEmpty()) "$greg · $hijri" else greg,
                    style = JannahType.caption,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            
            Text("NEXT PRAYER", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
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
            
            // LIVE COUNTDOWN
            LiveCountdown(displayTime)
            
            Spacer(Modifier.height(4.dp))
            Text(
                if (isAfterIsha) "Tomorrow" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(displayTime)),
                style = JannahType.caption,
                color = JannahColors.Muted
            )

            Spacer(Modifier.height(JannahSpacing.l))

            val level = TahajjudPlan.level(ctx)
            val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Orange)
                    .pressClickable { onNavigate("you") }
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

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .pressClickable { onNavigate("quran") }
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

            val h = DailyHadith.today()
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
                    Text(h.source, style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
fun LiveCountdown(targetTimeMs: Long) {
    var timeLeft by remember { mutableLongStateOf(targetTimeMs - System.currentTimeMillis()) }
    
    // This LaunchedEffect runs exactly once, updating the state every second
    LaunchedEffect(targetTimeMs) {
        while (true) {
            delay(1000L)
            val now = System.currentTimeMillis()
            timeLeft = (targetTimeMs - now).coerceAtLeast(0L)
        }
    }
    
    val h = timeLeft / 3600000
    val m = (timeLeft / 60000) % 60
    val s = (timeLeft / 1000) % 60
    Text(
        text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
        fontFamily = FontFamily.Serif,
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        color = JannahColors.Ink,
        letterSpacing = (-1).sp,
        lineHeight = 48.sp
    )
}
