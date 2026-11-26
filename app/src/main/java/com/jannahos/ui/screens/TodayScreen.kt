package com.jannahos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.*
import com.jannahos.SettingsActivity
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahColors.Ink
import com.jannahos.ui.theme.JannahColors.Muted
import com.jannahos.ui.theme.JannahColors.Orange
import com.jannahos.ui.theme.JannahColors.OrangeDeep
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(onNavigate: (String) -> Unit = {}) {
    val ctx = LocalContext.current
    val userName = remember { runCatching { Settings.displayName(ctx) }.getOrDefault("") }
    val lat = runCatching { Settings.lat(ctx) }.getOrDefault(0.0)
    val lon = runCatching { Settings.lon(ctx) }.getOrDefault(0.0)
    
    val prayerTimes = runCatching { SalahScheduler.todayTimes(ctx, lat, lon) }.getOrDefault(emptyMap())
    val isLocationSet = prayerTimes.isNotEmpty()
    
    val hadith = remember { runCatching { DailyHadith.today() }.getOrNull() }
    val hijri = remember { runCatching { HijriDate.today() }.getOrDefault("") }
    
    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (prayerTimes[it] ?: 0L) > now }
    val displayLabel = runCatching { nextKey?.let { SalahTracker.label(it) } ?: "Fajr" }.getOrDefault("Fajr")
    val displayTime = runCatching {
        if (nextKey == null) (prayerTimes["fajr"] ?: now) + 24L * 60 * 60 * 1000
        else prayerTimes[nextKey] ?: (now + 3600000)
    }.getOrDefault(now + 3600000)
    
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Peaceful night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    
    Column(Modifier.fillMaxSize().background(JannahColors.Cream).verticalScroll(rememberScrollState())) {
        
        // LOCATION WARNING
        if (!isLocationSet) {
            Box(
                Modifier.fillMaxWidth()
                    .background(JannahColors.Danger.copy(alpha = 0.1f))
                    .pressClickable { 
                        ctx.startActivity(android.content.Intent(ctx, SettingsActivity::class.java)) 
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Text("LOCATION NOT SET", style = JannahType.eyebrow, color = JannahColors.Danger)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap here to set your city for accurate prayer times.", style = JannahType.caption, color = Ink)
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 40.dp, bottom = 28.dp)) {
            Text("YOUR DAY", style = JannahType.eyebrow, color = OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text(greeting, fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Ink)
            if (userName.isNotBlank()) {
                Text("$userName.", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Spacer(Modifier.height(8.dp))
            val greg = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            Text(if (hijri.isNotBlank()) "$greg · $hijri" else greg, style = JannahType.eyebrow, color = Orange)
        }
        
        Column(Modifier.padding(horizontal = 22.dp)) {
            Text("NEXT PRAYER", style = JannahType.eyebrow, color = OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(displayLabel, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Orange)
                Spacer(Modifier.width(8.dp))
                Text("in", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 22.sp, color = Orange)
            }
            Spacer(Modifier.height(4.dp))
            AnimatedCountdown(displayTime)
            
            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))
            
            if (hadith != null) {
                Row {
                    Box(Modifier.width(3.dp).height(80.dp).background(Orange))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("HADITH", style = JannahType.eyebrow, color = OrangeDeep)
                        Spacer(Modifier.height(8.dp))
                        Text(hadith.english, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Body, lineHeight = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(hadith.source, style = JannahType.caption, color = Muted)
                    }
                }
                Spacer(Modifier.height(32.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                Spacer(Modifier.height(32.dp))
            }
            
            Row(Modifier.fillMaxWidth().pressClickable { onNavigate("sanctuary") }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Enter Sanctuary", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
                Text("→", style = JannahType.title, color = Orange)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            
            Row(Modifier.fillMaxWidth().pressClickable { onNavigate("quran") }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Continue reading Quran", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OrangeDeep, modifier = Modifier.weight(1f))
                Text("→", style = JannahType.title, color = Orange)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun AnimatedCountdown(targetTimeMs: Long) {
    var timeLeft by remember { mutableLongStateOf((targetTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)) }
    // Subtle fade animation for the ticking effect
    val alphaAnim by animateFloatAsState(
        targetValue = if (timeLeft % 2000 < 1000) 1f else 0.7f,
        animationSpec = tween(500),
        label = "tick"
    )
    
    LaunchedEffect(targetTimeMs) {
        while (true) {
            timeLeft = (targetTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(1000)
        }
    }
    val h = timeLeft / 3600000
    val m = (timeLeft / 60000) % 60
    val s = (timeLeft / 1000) % 60
    Text(
        text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
        fontFamily = FontFamily.Serif,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = Ink,
        letterSpacing = (-1).sp,
        modifier = Modifier.alpha(alphaAnim)
    )
}
