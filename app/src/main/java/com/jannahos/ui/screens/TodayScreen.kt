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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    var prayerTimes by remember { mutableStateOf(SalahScheduler.localTimes(Settings.lat(ctx), Settings.lon(ctx))) }
    var todayKey by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

    // Poll for date rollover; when it changes, todayKey updates and the fetch below re-fires.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val fresh = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (fresh != todayKey) todayKey = fresh
        }
    }

    LaunchedEffect(todayKey) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val fresh = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                prayerTimes = fresh
            }
        }
    }

    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it] ?: 0L) > now }
    val displayLabel = if (nextKey == null) "Fajr" else SalahTracker.label(nextKey)
    val displayTime = if (nextKey == null) (prayerTimes["fajr"] ?: now) + 24L * 60 * 60 * 1000 else prayerTimes[nextKey] ?: now

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Peaceful night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    val userName = Settings.displayName(ctx)
    val hadith = DailyHadith.today()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            Image(painter = painterResource(id = com.jannahos.R.drawable.hero_today), contentDescription = "Today", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.m)) {
                Text(if (userName.isBlank()) greeting + "." else "$greeting, $userName.", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                val greg = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
                val hijri = HijriDate.today()
                Text(if (hijri.isNotEmpty()) "$greg · $hijri" else greg, style = JannahType.caption, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Text("NEXT PRAYER", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(displayLabel, fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.width(8.dp))
                Text("in", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 20.sp, color = JannahColors.Orange, modifier = Modifier.padding(bottom = 5.dp))
            }
            Spacer(Modifier.height(2.dp))
            LiveCountdown(displayTime)
            Spacer(Modifier.height(4.dp))
            Text(if (nextKey == null) "Tomorrow" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(displayTime)), style = JannahType.caption, color = JannahColors.Muted)

            Spacer(Modifier.height(JannahSpacing.l))

            val ramadan = RamadanMode.current()
            if (ramadan.active) {
                val nowT = System.currentTimeMillis()
                val fajrTime = prayerTimes["fajr"] ?: nowT
                val maghribTime = prayerTimes["maghrib"] ?: nowT

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).pressClickable { onNavigate("ramadan") }.padding(JannahSpacing.l)) {
                    Column {
                        Text("RAMADAN · DAY ${ramadan.dayOfRamadan}", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(8.dp))

                        if (nowT < fajrTime) {
                            Text("Suhoor ends in", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.OnOrange)
                            val diff = fajrTime - nowT
                            val h = diff / 3600000
                            val m = (diff / 60000) % 60
                            Text(String.format(Locale.US, "%02dh %02dm", h, m), fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                        } else if (nowT < maghribTime) {
                            Text("Iftar in", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.OnOrange)
                            val diff = maghribTime - nowT
                            val h = diff / 3600000
                            val m = (diff / 60000) % 60
                            Text(String.format(Locale.US, "%02dh %02dm", h, m), fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)

                            Spacer(Modifier.height(16.dp))
                            var isFasted by remember { mutableStateOf(RamadanStore.isFastedToday(ctx)) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).clip(RoundedCornerShape(50)).background(if (isFasted) JannahColors.OnOrange else JannahColors.OnOrange.copy(alpha = 0.3f)).pressClickable {
                                    val next = !isFasted
                                    RamadanStore.setFastedToday(ctx, next)
                                    isFasted = next
                                }, contentAlignment = Alignment.Center) {
                                    if (isFasted) Text("✓", color = JannahColors.OrangeDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("I am fasting today", style = JannahType.bodySmall, color = JannahColors.OnOrange)
                            }
                        } else {
                            Text("Iftar complete.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                            Spacer(Modifier.height(4.dp))
                            Text("May Allah accept your fast.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = JannahColors.OnOrange.copy(alpha = 0.9f))
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Card).padding(JannahSpacing.l)) {
                Column {
                    Text("HADITH · ${hadith.theme.uppercase()}", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(hadith.english, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = JannahColors.Body, lineHeight = 24.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(hadith.source, style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            val latestEntry = remember { com.jannahos.DiaryStore(ctx).recent(1).firstOrNull() }
            if (latestEntry != null && (System.currentTimeMillis() - latestEntry.ts) < 24 * 60 * 60 * 1000) {
                val result = com.jannahos.DiaryClassifier.classify(latestEntry.text)
                if (result.reactionBody.isNotBlank()) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.OrangeSoft).padding(JannahSpacing.l)) {
                        Column {
                            Text(result.reactionTitle, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                            Spacer(Modifier.height(4.dp))
                            Text(result.reactionBody, style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 22.sp)
                            if (result.source.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(result.source, style = JannahType.micro, color = JannahColors.Muted)
                            }
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.m))
                }
            }

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Card).pressClickable { onNavigate("quran") }.padding(JannahSpacing.l)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("QURAN", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(6.dp))
                        Text("Continue reading", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
fun LiveCountdown(targetTimeMs: Long) {
    var timeLeft by remember { mutableLongStateOf(targetTimeMs - System.currentTimeMillis()) }
    LaunchedEffect(targetTimeMs) {
        while (true) {
            val now = System.currentTimeMillis()
            timeLeft = (targetTimeMs - now).coerceAtLeast(0L)
            delay(1000)
        }
    }
    val h = timeLeft / 3600000
    val m = (timeLeft / 60000) % 60
    val s = (timeLeft / 1000) % 60
    Text(text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s), fontFamily = FontFamily.Serif, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, letterSpacing = (-1).sp, lineHeight = 48.sp)
}
