package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import java.util.Date
import java.util.Locale

@Composable
fun PrayScreen() {
    val ctx = LocalContext.current
    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)

    var times by remember { mutableStateOf(SalahScheduler.todayTimes(lat, lon)) }
    var dateKey by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val fresh = System.currentTimeMillis()
            now = fresh
            val freshKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(fresh))
            if (freshKey != dateKey) {
                dateKey = freshKey
                times = SalahScheduler.todayTimes(lat, lon)
            }
        }
    }

    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (times[it] ?: 0L) > now }
    val isTomorrow = nextKey == null
    val nextTime: Long = nextKey?.let { times[it] } ?: ((times["fajr"] ?: 0L) + 24L * 60 * 60 * 1000L)
    val nextLabel = nextKey?.let { SalahTracker.label(it) } ?: "Fajr"
    val diffMs = (nextTime - now).coerceAtLeast(0L)
    val countdown = String.format(Locale.US, "%02d:%02d:%02d", diffMs / 3600000, (diffMs / 60000) % 60, (diffMs / 1000) % 60)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        Column(Modifier.padding(horizontal = JannahSpacing.gutter).padding(top = 64.dp, bottom = 32.dp)) {
            Text("Prayer", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text("Today for your location", style = JannahType.caption, color = JannahColors.Muted)
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            // Next Prayer Header
            Text(if (isTomorrow) "TOMORROW" else "NEXT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text(nextLabel, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(countdown, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
                Spacer(Modifier.width(8.dp))
                Text("· " + hhmm.format(Date(nextTime)), fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Muted, modifier = Modifier.padding(bottom = 3.dp))
            }

            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))

            // Clean List
            ordered.forEachIndexed { i, key ->
                val t = times[key]
                val isNext = key == nextKey
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(SalahTracker.label(key), fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal, color = if (isNext) JannahColors.Orange else JannahColors.Ink, modifier = Modifier.weight(1f))
                    Text(t?.let { hhmm.format(Date(it)) } ?: "—", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = if (isNext) JannahColors.Orange else JannahColors.Muted)
                }
                if (i < ordered.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            }

            Spacer(Modifier.height(32.dp))

            // Qibla Link
            Row(Modifier.fillMaxWidth().pressClickable { ctx.startActivity(Intent(ctx, QiblaActivity::class.java)) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Qibla direction", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, modifier = Modifier.weight(1f))
                Text("→", style = JannahType.title, color = JannahColors.Orange)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
