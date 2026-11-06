package com.jannahos

import android.content.Context.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    var times by remember { mutableStateOf(SalahScheduler.todayTimes(ctx, lat, lon)) }
    var dateKey by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Tahajjud State
    var tahajjudOn by remember { mutableStateOf(Settings.isTahajjudEnabled(ctx)) }
    var offset by remember { mutableStateOf(Settings.getTahajjudOffset(ctx)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val fresh = System.currentTimeMillis()
            now = fresh
            val freshKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(fresh))
            if (freshKey != dateKey) {
                dateKey = freshKey
                times = SalahScheduler.todayTimes(ctx, lat, lon)
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
            Text(if (isTomorrow) "TOMORROW" else "NEXT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text(nextLabel, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(countdown, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
                Spacer(Modifier.width(8.dp))
                Text("· " + hhmm.format(Date(nextTime)), fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Muted, modifier = Modifier.padding(bottom = 3.dp))
            }

            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))

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
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Spacer(Modifier.height(32.dp))

            // TAHAJJUD SECTION
            Text("NIGHT PRAYER", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tahajjud Alarm", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(2.dp))
                    Text(if (tahajjudOn) "Wakes you $offset minutes before Fajr" else "Off", style = JannahType.caption, color = JannahColors.Muted)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (tahajjudOn) JannahColors.Orange else JannahColors.Border)
                        .clickable {
                            tahajjudOn = !tahajjudOn
                            Settings.setTahajjudEnabled(ctx, tahajjudOn)
                            if (tahajjudOn) {
                                TahajjudScheduler.scheduleAlarm(ctx, offset)
                            } else {
                                TahajjudScheduler.cancel(ctx)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (tahajjudOn) "ON" else "OFF", style = JannahType.micro, color = if (tahajjudOn) JannahColors.OnOrange else JannahColors.Muted, fontWeight = FontWeight.Bold)
                }
            }

            if (tahajjudOn) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90).forEach { mins ->
                        val selected = offset == mins
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(50))
                                .background(if (selected) JannahColors.OrangeSoft else JannahColors.Card)
                                .clickable {
                                    offset = mins
                                    Settings.setTahajjudOffset(ctx, mins)
                                    TahajjudScheduler.scheduleAlarm(ctx, mins)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$mins m", style = JannahType.caption, color = if (selected) JannahColors.OrangeDeep else JannahColors.Muted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
