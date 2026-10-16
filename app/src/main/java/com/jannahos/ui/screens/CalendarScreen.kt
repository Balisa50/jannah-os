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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.IslamicCalendar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val events = remember { IslamicCalendar.upcoming(400) }
    val today = remember { IslamicCalendar.todayHijriLabel() }
    val fmt = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()) }

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
                Text("ISLAMIC CALENDAR",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Today: ", style = JannahType.title.copy(fontSize = 20.sp),
                        color = JannahColors.OnOrange)
                    Text(today, style = JannahType.titleItalic.copy(fontSize = 20.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Moon sighting may differ by a day.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("UPCOMING", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            if (events.isEmpty()) {
                Text("No upcoming events in the next 400 days.",
                    style = JannahType.bodySmall, color = JannahColors.Muted)
            } else {
                events.forEachIndexed { i, e ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                                .background(JannahColors.Card)
                                .padding(JannahSpacing.l)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Days away badge
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                        .background(
                                            when (e.kind) {
                                                "ramadan" -> JannahColors.Orange
                                                "eid" -> JannahColors.OrangeDeep
                                                "fast" -> JannahColors.OrangeSoft
                                                else -> JannahColors.SurfaceAlt
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        if (e.daysAway == 0L) "TODAY"
                                        else if (e.daysAway == 1L) "TOMORROW"
                                        else "${e.daysAway} days",
                                        style = JannahType.micro,
                                        color = when (e.kind) {
                                            "ramadan", "eid" -> JannahColors.OnOrange
                                            else -> JannahColors.Ink
                                        }
                                    )
                                }
                                Spacer(Modifier.width(JannahSpacing.s))
                                Text(e.hijriLabel,
                                    style = JannahType.micro,
                                    color = JannahColors.Muted)
                            }

                            Spacer(Modifier.height(JannahSpacing.s))

                            Text(e.name,
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink,
                                lineHeight = 24.sp)

                            Spacer(Modifier.height(JannahSpacing.xs))

                            Text(fmt.format(e.gregorianDate),
                                style = JannahType.bodySmall,
                                color = JannahColors.Muted)

                            if (e.note.isNotBlank()) {
                                Spacer(Modifier.height(JannahSpacing.m))
                                Text(e.note,
                                    style = JannahType.caption,
                                    color = JannahColors.Body,
                                    lineHeight = 18.sp)
                            }
                        }
                    }
                    if (i < events.size - 1) Spacer(Modifier.height(JannahSpacing.xs))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Note about scholarly honesty
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("A NOTE ON DATES",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "Islamic dates depend on the moon sighting. The dates here are computed from the tabular Hijri calendar and may be off by a day from your local authority. Always follow your local community.",
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
