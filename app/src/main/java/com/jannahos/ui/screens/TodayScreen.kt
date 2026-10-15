package com.jannahos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FrictionMap
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.TahajjudScheduler
import com.jannahos.WakeLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen() {
    val ctx = LocalContext.current
    val refresh = remember { mutableStateOf(0) }
    val ignore = refresh.value

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val next = TahajjudScheduler.computeTonight(lat, lon)
    val scheduled = TahajjudScheduler.nextScheduled(ctx)
    val fullFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val onTonight = TahajjudPlan.isOnTonight(ctx)

    val insight = FrictionMap.topInsight(ctx)
    val prayed7 = FrictionMap.prayedLast7Days(ctx)
    val log = WakeLog(ctx)
    val history = log.lastNights(7)
    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.m),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Tonight", style = JannahType.screenTitle, color = JannahColors.DayText)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Your tahajjud plan for tonight.", style = JannahType.secondary, color = JannahColors.DayMuted)

        Spacer(Modifier.height(JannahSpacing.m))

        // ---- Hero card: tonight's time + primary action ----
        Card {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("Last third begins", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(fullFmt.format(next), color = JannahColors.DayText,
                    fontSize = 26.sp, fontWeight = FontWeight.Light)

                Spacer(Modifier.height(JannahSpacing.m))

                Button(
                    onClick = {
                        TahajjudScheduler.schedule(ctx, next)
                        refresh.value = refresh.value + 1
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.Accent,
                        contentColor = JannahColors.AccentInk
                    )
                ) { Text("Set tahajjud alarm", style = JannahType.button) }

                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    if (scheduled > 0L) "Alarm set · " + fullFmt.format(Date(scheduled))
                    else "No alarm set",
                    style = JannahType.tertiary, color = JannahColors.DayMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---- Formation card ----
        Spacer(Modifier.height(JannahSpacing.m))
        Card {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("Formation", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    when (level) {
                        1 -> "Level 1 · One night a week"
                        2 -> "Level 2 · Two nights a week"
                        3 -> "Level 3 · Three nights a week"
                        else -> "Level 4 · Every night"
                    },
                    style = JannahType.primary, color = JannahColors.DayText
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    (if (onTonight) "Scheduled tonight · " else "Rest tonight · ") +
                        "$prayedWeek of $target this week",
                    style = JannahType.secondary, color = JannahColors.DayMuted
                )
            }
        }

        // ---- Pattern card ----
        if (prayed7 > 0 || insight != null) {
            Spacer(Modifier.height(JannahSpacing.m))
            Card {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("Pattern", style = JannahType.section, color = JannahColors.DayMuted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    if (prayed7 > 0) {
                        Text("$prayed7 of last 7 nights prayed",
                            style = JannahType.primary, color = JannahColors.DayText)
                        Spacer(Modifier.height(JannahSpacing.xs))
                    }
                    if (insight != null) {
                        Text("Common block · ${insight.label}",
                            style = JannahType.secondary, color = JannahColors.DayMuted)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(insight.suggestion, style = JannahType.secondary,
                            color = JannahColors.Accent, lineHeight = 20.sp)
                    }
                }
            }
        }

        // ---- Recent nights card ----
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(JannahSpacing.m))
            Card {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("Last 7 nights", style = JannahType.section, color = JannahColors.DayMuted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    history.forEach { e ->
                        val day = dayFmt.format(Date(e.ts))
                        val status = when (e.outcome) {
                            "prayed" -> "Prayed"
                            "missed" -> "Missed"
                            else -> e.outcome
                        }
                        val sc = if (e.outcome == "prayed") JannahColors.Accent else JannahColors.DayMuted
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day, style = JannahType.secondary, color = JannahColors.DayMuted)
                            Text(status, style = JannahType.secondary, color = sc)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(JannahColors.DaySurface)
    ) { content() }
}
