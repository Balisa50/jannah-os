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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
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
    var tick by remember { mutableStateOf(0) }
    var reminderMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val times = SalahScheduler.todayTimes(lat, lon)
    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val today = SalahTracker.today(ctx)
    val done = today.count { it.value }
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    val now = System.currentTimeMillis()
    val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { times[it] }?.time

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Banner
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("SALAH",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("$done of 5 ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("prayed.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Next prayer hero
            if (nextKey != null && nextTime != null) {
                Text("Next", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    SalahTracker.label(nextKey),
                    style = JannahType.display,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    hhmm.format(Date(nextTime)),
                    style = JannahType.title.copy(fontSize = 22.sp),
                    color = JannahColors.Orange
                )
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Times list
            Text("TODAY'S TIMES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            ordered.forEachIndexed { i, key ->
                val t = times[key]
                val done = today[key] == true
                Row(
                    Modifier
                        .fillMaxWidth()
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
                            fontFamily = FontFamily.Serif,
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

            Spacer(Modifier.height(JannahSpacing.l))

            // Set reminders pill
            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        if (!SalahScheduler.canUseExactAlarms(ctx)) {
                            reminderMsg = "Enable Alarms & reminders in system settings."
                        } else {
                            SalahScheduler.schedule(ctx, lat, lon)
                            reminderMsg = "Reminders set for today."
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("Set prayer reminders  →",
                    style = JannahType.button, color = JannahColors.OnOrange)
            }

            if (reminderMsg != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(reminderMsg!!, style = JannahType.caption, color = JannahColors.Orange)
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Mark today
            Text("MARK TODAY", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            ordered.forEachIndexed { i, key ->
                val done = today[key] == true
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        SalahTracker.label(key),
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (done) JannahColors.Orange else JannahColors.Card)
                            .clickable { SalahTracker.toggle(ctx, key); tick++ }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            if (done) "✓  Prayed" else "Mark",
                            style = JannahType.caption,
                            color = if (done) JannahColors.OnOrange else JannahColors.Muted
                        )
                    }
                }
                if (i < ordered.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            Text(
                "A private record. No streak resets. No punishment.",
                style = JannahType.caption, color = JannahColors.Faint,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
