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
import androidx.compose.foundation.layout.width
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
import com.jannahos.SleepLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val log = remember { SleepLog(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val today = log.today()
    val nights = log.lastNights(7)
    val avgHours = log.averageHours(7)

    // Default bed / wake times — 22:30 and 05:00 in minutes-from-midnight
    var bedHour by remember { mutableStateOf(today?.let { hourOf(it.bedtime) } ?: 22) }
    var bedMin by remember { mutableStateOf(today?.let { minOf(it.bedtime) } ?: 30) }
    var wakeHour by remember { mutableStateOf(today?.let { hourOf(it.waketime) } ?: 5) }
    var wakeMin by remember { mutableStateOf(today?.let { minOf(it.waketime) } ?: 0) }
    var quality by remember { mutableStateOf(today?.quality ?: 3) }

    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

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
                Text("SLEEP",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("The base of ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("tahajjud.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (avgHours > 0f)
                        "%.1f h average over the last 7 nights".format(avgHours)
                    else "Log tonight to start the pattern",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("LAST NIGHT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    // Bedtime row
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bedtime",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                            TimeStepper(
                                label = "--",
                                onClick = {
                                    var total = bedHour * 60 + bedMin - 15
                                    if (total < 0) total += 24 * 60
                                    bedHour = (total / 60) % 24; bedMin = total % 60
                                }
                            )
                            TimeDisplay("%02d:%02d".format(bedHour, bedMin))
                            TimeStepper(
                                label = "++",
                                onClick = {
                                    var total = bedHour * 60 + bedMin + 15
                                    total %= 24 * 60
                                    bedHour = total / 60; bedMin = total % 60
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.m))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    Spacer(Modifier.height(JannahSpacing.m))

                    // Waketime row
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Wake",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                            TimeStepper(
                                label = "--",
                                onClick = {
                                    var total = wakeHour * 60 + wakeMin - 15
                                    if (total < 0) total += 24 * 60
                                    wakeHour = (total / 60) % 24; wakeMin = total % 60
                                }
                            )
                            TimeDisplay("%02d:%02d".format(wakeHour, wakeMin))
                            TimeStepper(
                                label = "++",
                                onClick = {
                                    var total = wakeHour * 60 + wakeMin + 15
                                    total %= 24 * 60
                                    wakeHour = total / 60; wakeMin = total % 60
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.m))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    Spacer(Modifier.height(JannahSpacing.m))

                    // Quality 1..5
                    Text("Quality", fontFamily = FontFamily.Serif,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                    ) {
                        (1..5).forEach { q ->
                            val selected = quality == q
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(if (selected) JannahColors.Orange else JannahColors.SurfaceAlt)
                                    .clickable { quality = q },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$q",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) JannahColors.OnOrange else JannahColors.Ink)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        val bed = toMillis(bedHour, bedMin, isBedtime = true)
                        val wake = toMillis(wakeHour, wakeMin, isBedtime = false)
                        log.save(bed, wake, quality)
                        refresh = refresh + 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Save last night", style = JannahType.button,
                    color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Past week rows
            if (nights.isNotEmpty()) {
                Text("LAST 7 NIGHTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                nights.forEachIndexed { i, e ->
                    val day = e.day
                    val hours = (e.waketime - e.bedtime) / (1000.0 * 60 * 60)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(4.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (hours >= 7) JannahColors.Orange
                                        else JannahColors.Border
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(day, fontFamily = FontFamily.Serif,
                                fontSize = 17.sp, fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                        }
                        Text("%.1f h".format(hours),
                            style = JannahType.body,
                            color = JannahColors.Muted)
                    }
                    if (i < nights.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Note
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("WHY THIS MATTERS",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "Tahajjud depends on when you sleep. The nights you log early bedtime are the nights your wake-up holds. Track it. Do not punish the misses.",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                }
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

@Composable
private fun TimeStepper(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.SurfaceAlt)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = JannahType.body, color = JannahColors.Muted)
    }
}

@Composable
private fun TimeDisplay(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Cream)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text,
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
    }
}

private fun hourOf(ms: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    return c.get(Calendar.HOUR_OF_DAY)
}

private fun minOf(ms: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    return c.get(Calendar.MINUTE)
}

// isBedtime = true means the time is last night's; wake = today.
private fun toMillis(hour: Int, minute: Int, isBedtime: Boolean): Long {
    val c = Calendar.getInstance()
    if (isBedtime) c.add(Calendar.DAY_OF_YEAR, -1)
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, minute)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}
