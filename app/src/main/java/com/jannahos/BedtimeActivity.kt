package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jannahos.ui.SwipeBackScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BedtimeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SwipeBackScreen(onBack = { finish() }) { BedtimeScreen() }
                }
            }
        }
    }

    @Composable
    private fun BedtimeScreen() {
        val (h0, m0) = BedtimeStore.get(this)
        var hour by remember { mutableStateOf(h0) }
        var minute by remember { mutableStateOf(m0) }

        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Tonight's plan", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("I intend to sleep at", style = JannahType.section, color = JannahColors.DayMuted)
            Spacer(Modifier.height(JannahSpacing.m))
            Text(fmt.format(hourMinToDate(hour, minute)),
                style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.l))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                Button(
                    onClick = {
                        var total = hour * 60 + minute - 15
                        if (total < 0) total += 24 * 60
                        hour = (total / 60) % 24
                        minute = total % 60
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.DaySurface,
                        contentColor = JannahColors.DayText
                    )
                ) { Text("− 15m", style = JannahType.secondary) }

                Button(
                    onClick = {
                        var total = hour * 60 + minute + 15
                        total %= 24 * 60
                        hour = total / 60
                        minute = total % 60
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.DaySurface,
                        contentColor = JannahColors.DayText
                    )
                ) { Text("+ 15m", style = JannahType.secondary) }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("A reminder", style = JannahType.section, color = JannahColors.DayMuted)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("Surah Al-Mulk before sleep", style = JannahType.primary, color = JannahColors.DayText)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "\"Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.\" (Jami' at-Tirmidhi 2891; graded hasan by some scholars, da'if by others, differences exist.)",
                style = JannahType.tertiary,
                color = JannahColors.DayMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Button(
                onClick = {
                    BedtimeStore.set(this@BedtimeActivity, hour, minute)
                    BedtimeScheduler.schedule(this@BedtimeActivity)
                    finish()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("Save and return", style = JannahType.button) }

            Spacer(Modifier.height(JannahSpacing.m))

            TextButton(onClick = { finish() }) {
                Text("Cancel", style = JannahType.tertiary, color = JannahColors.DayMuted)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }

    private fun hourMinToDate(h: Int, m: Int): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, h)
        c.set(Calendar.MINUTE, m)
        c.set(Calendar.SECOND, 0)
        return c.time
    }
}
