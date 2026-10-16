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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jannahos.DeedsEngine
import com.jannahos.SalahScheduler
import com.jannahos.Settings
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun TimeScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var selectedMinutes by remember { mutableStateOf<Int?>(null) }
    var seed by remember { mutableStateOf(0) }

    // Current next-prayer gap (real, not decorative)
    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val times = try { SalahScheduler.todayTimes(lat, lon) } catch (e: Exception) { emptyMap() }
    val now = System.currentTimeMillis()
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
    val nextMs = nextKey?.let { times[it] }?.time
    val gapMin = if (nextMs != null) ((nextMs - now) / 60_000L).toInt() else 0
    val nextLabel = nextKey?.let { com.jannahos.SalahTracker.label(it) } ?: "—"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("YOUR TIME",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("No minute ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("is empty.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Every act here is from a verified hadith.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Live context: how long until next prayer
            if (nextMs != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("TIME UNTIL ${nextLabel.uppercase()}",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$gapMin",
                                fontFamily = FontFamily.Serif,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink,
                                letterSpacing = (-1).sp,
                                lineHeight = 48.sp)
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Text("minutes left",
                                style = JannahType.title.copy(fontSize = 16.sp),
                                color = JannahColors.Muted,
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Window chooser
            Text("HOW MUCH TIME DO YOU HAVE?",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("Tap a window. See what a Muslim can earn inside it.",
                style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(JannahSpacing.m))

            val windows = listOf(
                1 to "1 minute",
                5 to "5 minutes",
                15 to "15 minutes"
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                windows.forEach { (mins, label) ->
                    val selected = selectedMinutes == mins
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (selected) JannahColors.Orange else JannahColors.Card)
                            .clickable {
                                selectedMinutes = mins
                                seed++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label,
                            style = JannahType.button,
                            color = if (selected) JannahColors.OnOrange else JannahColors.Ink)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Suggestion for the chosen window
            if (selectedMinutes != null) {
                val act = DeedsEngine.suggestFor(selectedMinutes!!, seed)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeDeep)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(DeedsEngine.tierLabel(act).uppercase(),
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.7f))
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(act.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange,
                            lineHeight = 28.sp)
                        Spacer(Modifier.height(JannahSpacing.m))
                        Text(act.reward,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = JannahColors.OnOrange.copy(alpha = 0.92f),
                            lineHeight = 22.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(act.source,
                            style = JannahType.micro,
                            color = JannahColors.OnOrange.copy(alpha = 0.65f))
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.OnOrange.copy(alpha = 0.18f))
                                .clickable { seed++ }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Show another",
                                style = JannahType.caption,
                                color = JannahColors.OnOrange,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // All acts list
            Text("WHAT A MUSLIM CAN DO",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            DeedsEngine.acts.forEachIndexed { i, act ->
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(JannahColors.OrangeSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("${act.minutes}m",
                                style = JannahType.micro, color = JannahColors.OrangeDeep)
                        }
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(act.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(act.reward,
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(act.source,
                        style = JannahType.micro, color = JannahColors.Faint)
                }
                if (i < DeedsEngine.acts.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // The framing
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Text(
                    "The Quran says: \"So whoever does an atom's weight of good will see it.\" (Qur'an 99:7). A minute spent well is not wasted. It is recorded.",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    color = JannahColors.Body,
                    lineHeight = 24.sp
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
