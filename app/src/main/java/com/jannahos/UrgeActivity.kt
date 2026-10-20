package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class UrgeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme() {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UrgeFlow()
                }
            }
        }
    }

    @Composable
    private fun UrgeFlow() {
        var step by remember { mutableStateOf(0) }
        val log = remember { StruggleLog(this@UrgeActivity) }
        val reminder = remember { Mercy.random() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text("Not now.", style = JannahType.screenTitle, color = JannahColors.NightText)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text("Breathe. This urge will pass.",
                        style = JannahType.primary, color = JannahColors.NightMuted,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.xl))
                    Text(reminder.text, style = JannahType.primary, color = JannahColors.NightText,
                        textAlign = TextAlign.Center, lineHeight = 24.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(reminder.source, style = JannahType.tertiary, color = JannahColors.NightMuted)
                    Spacer(Modifier.height(JannahSpacing.xxl))
                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.Orange,
                            contentColor = JannahColors.OnOrange
                        )
                    ) { Text("I am still struggling", style = JannahType.button) }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Button(
                        onClick = {
                            log.record("resisted")
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.NightSurface,
                            contentColor = JannahColors.NightText
                        )
                    ) { Text("The urge is fading", style = JannahType.secondary) }
                    Spacer(Modifier.height(JannahSpacing.s))
                    TextButton(onClick = { finish() }) {
                        Text("Close", style = JannahType.tertiary, color = JannahColors.NightMuted)
                    }
                }
                1 -> {
                    Text("Do something else.", style = JannahType.screenTitle, color = JannahColors.NightText)
                    Spacer(Modifier.height(JannahSpacing.l))
                    Text("Stand up. Drink water. Change rooms.\nMake wudu. Step outside.",
                        style = JannahType.primary, color = JannahColors.NightMuted,
                        textAlign = TextAlign.Center, lineHeight = 26.sp)
                    Spacer(Modifier.height(JannahSpacing.xl))
                    Text(reminder.text, style = JannahType.secondary, color = JannahColors.NightText,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(reminder.source, style = JannahType.tertiary, color = JannahColors.NightMuted)
                    Spacer(Modifier.height(JannahSpacing.xl))
                    Button(
                        onClick = {
                            log.record("resisted")
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.Orange,
                            contentColor = JannahColors.OnOrange
                        )
                    ) { Text("I got through it", style = JannahType.button) }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Button(
                        onClick = {
                            log.record("slipped")
                            step = 3
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.NightSurface,
                            contentColor = JannahColors.NightText
                        )
                    ) { Text("I slipped", style = JannahType.secondary) }
                }
                2 -> {
                    Text("Alhamdulillah.", style = JannahType.screenTitle, color = JannahColors.NightText)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text("That moment was a choice. You made it.",
                        style = JannahType.primary, color = JannahColors.NightMuted,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.xxl))
                    Button(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.Orange,
                            contentColor = JannahColors.OnOrange
                        )
                    ) { Text("Return", style = JannahType.button) }
                }
                3 -> {
                    Text("Return to Allah.", style = JannahType.screenTitle, color = JannahColors.NightText,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text("Say Astaghfirullah, intend not to repeat, and do not despair.",
                        style = JannahType.secondary, color = JannahColors.NightMuted,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.l))
                    Text(reminder.text, style = JannahType.secondary, color = JannahColors.NightText,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(reminder.source, style = JannahType.tertiary, color = JannahColors.NightMuted)
                    Spacer(Modifier.height(JannahSpacing.xl))
                    Button(
                        onClick = {
                            log.record("tawbah")
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.Orange,
                            contentColor = JannahColors.OnOrange
                        )
                    ) { Text("I have repented", style = JannahType.button) }
                    Spacer(Modifier.height(JannahSpacing.s))
                    TextButton(onClick = { finish() }) {
                        Text("Close", style = JannahType.tertiary, color = JannahColors.NightMuted)
                    }
                }
            }
        }
    }
}
