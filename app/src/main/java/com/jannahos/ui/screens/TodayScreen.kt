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
    var prayerTimes by remember { mutableStateOf(SalahScheduler.localTimes(lat, lon)) }
    
    LaunchedEffect(todayKey, lat, lon) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // This calls the API and falls back to local automatically
            val freshTimes = SalahScheduler.todayTimes(lat, lon)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                prayerTimes = freshTimes
            }
        }
    }
    
    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it] ?: 0L) > now }
    val nextTime = nextKey?.let { prayerTimes[it] }

    val isAfterIsha = nextKey == null
    val displayLabel = if (isAfterIsha) "Fajr" else SalahTracker.label(nextKey!!)
    val displayTime = if (isAfterIsha) {
        (prayerTimes["fajr"] ?: now) + 24L * 60 * 60 * 1000
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
                Spacer(Modifier.height(8.dp))
                Text(
                    RightNow.getMessage(),
                    style = JannahType.caption,
                    color = JannahColors.Orange,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            
            // DYNAMIC DIARY CARD
            val latestEntry = remember { com.jannahos.DiaryStore(ctx).recent(1).firstOrNull() }
            if (latestEntry != null && (System.currentTimeMillis() - latestEntry.ts) < 24 * 60 * 60 * 1000) {
                val result = com.jannahos.DiaryClassifier.classify(latestEntry.text)
                if (result.reactionBody.isNotBlank()) {
                    Spacer(Modifier.height(JannahSpacing.m))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.OrangeSoft)
                            .padding(JannahSpacing.l)
                    ) {
                        Column {
                            Text(result.reactionTitle, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                            Spacer(Modifier.height(4.dp))
                            Text(result.reactionBody, style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 22.sp)
                            if (result.source.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(result.source, style = JannahType.micro, color = JannahColors.Muted)
                            }
                            if (result.duaRecommendation != null) {
                                Spacer(Modifier.height(12.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.OrangeDeep.copy(alpha = 0.2f)))
                                Spacer(Modifier.height(12.dp))
                                Text("Recommended Dua", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(result.duaRecommendation, style = JannahType.body, color = JannahColors.Ink, lineHeight = 24.sp)
                            }
                        }
                    }
                }
            }
            

            Spacer(Modifier.height(JannahSpacing.l))

            // SADAQAH DEBT
            val debt = SadaqahStore.getDebt(ctx)
            if (debt > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("SADAQAH DEBT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(4.dp))
                        Text("You owe \$$debt to charity.", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        Spacer(Modifier.height(4.dp))
                        Text("Missed prayers accumulate a $1 penalty. Give it away to clear it.", style = JannahType.caption, color = JannahColors.Muted)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(JannahColors.OrangeSoft)
                                .pressClickable { SadaqahStore.clearDebt(ctx) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("I paid it", style = JannahType.caption, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // RAMADAN MODE
            val ramadan = RamadanMode.current()
            if (ramadan.active) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Orange)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("RAMADAN · DAY ${ramadan.dayOfRamadan}", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(8.dp))
                        
                        // Check if it's before Fajr (Suhoor) or after (Iftar)
                        val now = System.currentTimeMillis()
                        val fajrTime = prayerTimes["fajr"] ?: now
                        val maghribTime = prayerTimes["maghrib"] ?: now
                        
                        if (now < fajrTime) {
                            Text("Suhoor ends in", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.OnOrange)
                            val diff = fajrTime - now
                            val h = diff / 3600000
                            val m = (diff / 60000) % 60
                            Text(String.format(Locale.US, "%02dh %02dm", h, m), fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                        } else if (now < maghribTime) {
                            Text("Iftar in", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.OnOrange)
                            val diff = maghribTime - now
                            val h = diff / 3600000
                            val m = (diff / 60000) % 60
                            Text(String.format(Locale.US, "%02dh %02dm", h, m), fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                        } else {
                            Text("Iftar complete.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                            Spacer(Modifier.height(4.dp))
                            Text("May Allah accept your fast.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = JannahColors.OnOrange.copy(alpha = 0.9f))
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("FASTING TRACKER", style = JannahType.micro, color = JannahColors.OnOrange.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        
                        // Simple 10x3 grid for 30 days
                        for (row in 0 until 3) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (col in 1..10) {
                                    val dayNum = row * 10 + col
                                    val isCompleted = dayNum < ramadan.dayOfRamadan
                                    val isToday = dayNum == ramadan.dayOfRamadan
                                    
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    isCompleted -> JannahColors.OnOrange
                                                    isToday -> JannahColors.OnOrange.copy(alpha = 0.5f)
                                                    else -> JannahColors.OnOrange.copy(alpha = 0.1f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$dayNum", fontSize = 10.sp, color = if (isCompleted) JannahColors.OrangeDeep else JannahColors.OnOrange)
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

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
