package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val fresh = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (fresh != todayKey) todayKey = fresh
        }
    }

    LaunchedEffect(todayKey) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val fresh = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    prayerTimes = fresh
                }
            } catch (e: Exception) {
                // Ignore errors, keep default times
            }
        }
    }

    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it] ?: 0L) > now }
    val displayLabel = if (nextKey == null) "Fajr" else SalahTracker.label(nextKey)
    // If next prayer is tomorrow (null), add 24 hours to today's Fajr time
    val displayTime = if (nextKey == null) {
        val todayFajr = prayerTimes["fajr"] ?: now
        todayFajr + 24L * 60 * 60 * 1000L
    } else {
        prayerTimes[nextKey] ?: now
    }

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
        // Seamless Header
        Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).padding(top = 64.dp, bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (userName.isBlank()) greeting else "$greeting, $userName.", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, lineHeight = 42.sp, modifier = Modifier.weight(1f))
                val score = com.jannahos.IntegrityStore.getFocusScore(ctx)
                Text("$score%", style = JannahType.title, color = if (score > 80) JannahColors.Success else JannahColors.Danger)
            }
            Spacer(Modifier.height(8.dp))
            val greg = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            val hijri = HijriDate.today()
            Text(if (hijri.isNotEmpty()) "$greg · $hijri" else greg, style = JannahType.eyebrow, color = JannahColors.Orange)
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            // Next Prayer Block
            Text("NEXT PRAYER", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(displayLabel, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
                Spacer(Modifier.width(8.dp))
                Text("in", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 22.sp, color = JannahColors.Orange, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(4.dp))
            LiveCountdown(displayTime)
            
            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))

            // RAMADAN HERO SECTION
            val ramadan = RamadanMode.current()
            if (ramadan.active) {
                val maghribTime = prayerTimes["maghrib"] ?: now
                val fajrTime = prayerTimes["fajr"] ?: now
                
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(JannahColors.Orange).padding(24.dp)) {
                    Column {
                        Text("RAMADAN · DAY ${ramadan.dayOfRamadan}", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(12.dp))
                        
                        if (now < fajrTime) {
                            Text("Suhoor ends in", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = JannahColors.OnOrange)
                            val diff = fajrTime - now
                            Text(String.format(Locale.US, "%02dh %02dm", diff / 3600000, (diff / 60000) % 60), fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                        } else if (now < maghribTime) {
                            Text("Iftar in", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = JannahColors.OnOrange)
                            val diff = maghribTime - now
                            Text(String.format(Locale.US, "%02dh %02dm", diff / 3600000, (diff / 60000) % 60), fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                            
                            Spacer(Modifier.height(16.dp))
                            val isFasted = RamadanStore.isFastedToday(ctx)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).clip(RoundedCornerShape(50)).background(if (isFasted) JannahColors.OnOrange else JannahColors.OnOrange.copy(alpha = 0.3f)).pressClickable {
                                    RamadanStore.setFastedToday(ctx, !isFasted)
                                }, contentAlignment = Alignment.Center) {
                                    if (isFasted) Text("✓", color = JannahColors.OrangeDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("I am fasting today", style = JannahType.body, color = JannahColors.OnOrange)
                            }
                        } else {
                            Text("Iftar complete.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                Spacer(Modifier.height(32.dp))
            }

            // Hadith
            Row {
                Box(Modifier.width(3.dp).height(80.dp).background(JannahColors.Orange))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("HADITH", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(8.dp))
                    Text(hadith.english, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Body, lineHeight = 28.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(hadith.source, style = JannahType.caption, color = JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))

            // Diary Integration
            val latestEntry = remember { com.jannahos.DiaryStore(ctx).recent(1).firstOrNull() }
            if (latestEntry != null && (System.currentTimeMillis() - latestEntry.ts) < 24 * 60 * 60 * 1000) {
                val result = com.jannahos.DiaryClassifier.classify(latestEntry.text)
                if (result.reactionBody.isNotBlank()) {
                    Text(result.reactionTitle, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(8.dp))
                    Text(result.reactionBody, style = JannahType.body, color = JannahColors.Ink, lineHeight = 24.sp)
                    Spacer(Modifier.height(32.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Sanctuary Hook
            Row(Modifier.fillMaxWidth().pressClickable { onNavigate("sanctuary") }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Enter Sanctuary", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, modifier = Modifier.weight(1f))
                Text("→", style = JannahType.title, color = JannahColors.Orange)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))

            // Quran Continue
            Row(Modifier.fillMaxWidth().pressClickable { onNavigate("quran") }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Continue reading Quran", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep, modifier = Modifier.weight(1f))
                Text("→", style = JannahType.title, color = JannahColors.Orange)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
        }
        Spacer(Modifier.height(JannahSpacing.xxl))
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
    Text(text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s), fontFamily = FontFamily.Serif, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, letterSpacing = (-1).sp, lineHeight = 52.sp)
}
