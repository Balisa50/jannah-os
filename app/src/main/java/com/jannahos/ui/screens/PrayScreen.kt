package com.jannahos.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.jannahos.QiblaActivity
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrayScreen() {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    var reminderMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val todayKey = remember { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date()) }
    val times = remember(todayKey, lat, lon) { SalahScheduler.todayTimes(lat, lon) }
    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val today = SalahTracker.today(ctx)
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val done = today.count { it.value }

    val now = System.currentTimeMillis()
    val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { times[it] }?.time

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.l, bottom = JannahSpacing.m)
        ) {
            Column {
                Text("Pray",
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink)
                Spacer(Modifier.height(2.dp))
                Text("$done of 5 marked",
                    style = JannahType.caption, color = JannahColors.Muted)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {

            // Next-prayer summary card (only if there is one)
            if (nextKey != null && nextTime != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Orange)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("NEXT",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(SalahTracker.label(nextKey),
                                fontFamily = FontFamily.Serif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.OnOrange)
                            Spacer(Modifier.width(8.dp))
                            Text(hhmm.format(nextTime),
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 18.sp,
                                color = JannahColors.OnOrange.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.m))
            }

            // Prayer list, name + time + status in one row
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
            ) {
                Column {
                    ordered.forEachIndexed { i, key ->
                        val t = times[key]
                        val isDone = today[key] == true
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { SalahTracker.toggle(ctx, key); tick++ }
                                .padding(horizontal = JannahSpacing.l, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(SalahTracker.label(key),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JannahColors.Ink)
                                Spacer(Modifier.height(2.dp))
                                Text(t?.let { hhmm.format(it) } ?: ", ",
                                    style = JannahType.bodySmall,
                                    color = JannahColors.Muted)
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(
                                        if (isDone) JannahColors.Orange else JannahColors.Cream
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    if (isDone) "✓ Marked" else "Mark",
                                    style = JannahType.caption,
                                    color = if (isDone) JannahColors.OnOrange else JannahColors.Muted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (i < ordered.size - 1) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                        }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            // Qibla, labelled card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable {
                        ctx.startActivity(Intent(ctx, QiblaActivity::class.java))
                    }
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Qibla direction",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text("Tap to open the compass",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            // Reminders
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        if (!SalahScheduler.canUseExactAlarms(ctx)) {
                            reminderMsg = "Enable Alarms & reminders in system settings."
                        } else {
                            SalahScheduler.schedule(ctx, lat, lon)
                            reminderMsg = "Reminders set for today."
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Set prayer reminders",
                    style = JannahType.button, color = JannahColors.OnOrange)
            }

            if (reminderMsg != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(reminderMsg!!,
                    style = JannahType.caption, color = JannahColors.Orange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }

        }
    }
}
