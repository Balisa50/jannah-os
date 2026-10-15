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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrayScreen() {
    val ctx = LocalContext.current
    val refresh = remember { mutableStateOf(0) }
    val ignore = refresh.value

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val times = SalahScheduler.todayTimes(lat, lon)
    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val today = SalahTracker.today(ctx)
    val done = today.count { it.value }

    val reminderMsg = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.m),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Pray", style = JannahType.screenTitle, color = JannahColors.DayText)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("$done of 5 today", style = JannahType.secondary, color = JannahColors.DayMuted)

        Spacer(Modifier.height(JannahSpacing.m))

        Card {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("Today's times", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.s))
                times.forEach { (key, t) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(SalahTracker.label(key), style = JannahType.primary, color = JannahColors.DayText)
                        Text(hhmm.format(t), style = JannahType.primary, color = JannahColors.DayMuted)
                    }
                }

                Spacer(Modifier.height(JannahSpacing.s))

                Button(
                    onClick = {
                        if (!SalahScheduler.canUseExactAlarms(ctx)) {
                            reminderMsg.value = "Enable Alarms & reminders in system settings."
                        } else {
                            SalahScheduler.schedule(ctx, lat, lon)
                            reminderMsg.value = "Reminders set for today."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.DayBg,
                        contentColor = JannahColors.DayText
                    )
                ) { Text("Set prayer reminders", style = JannahType.secondary) }

                if (reminderMsg.value != null) {
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(reminderMsg.value!!, style = JannahType.tertiary,
                        color = JannahColors.Accent, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Card {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("Mark today", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.s))

                today.forEach { (key, completed) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(SalahTracker.label(key), style = JannahType.primary, color = JannahColors.DayText)
                        if (completed) {
                            TextButton(onClick = {
                                SalahTracker.toggle(ctx, key)
                                refresh.value = refresh.value + 1
                            }) {
                                Text("✓ Prayed", style = JannahType.secondary, color = JannahColors.Accent)
                            }
                        } else {
                            TextButton(onClick = {
                                SalahTracker.toggle(ctx, key)
                                refresh.value = refresh.value + 1
                            }) {
                                Text("Mark", style = JannahType.secondary, color = JannahColors.DayMuted)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Text(
            "A private record. No streak resets. No punishment.",
            style = JannahType.tertiary, color = JannahColors.DayMuted,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

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
