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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NightBg = Color(0xFF07090D)
private val NightSurface = Color(0xFF161A22)
private val NightText = Color(0xFFE8E5DC)
private val NightMuted = Color(0xFF8B8F98)
private val GoldAccent = Color(0xFFC9A961)

private val Colors = darkColorScheme(
    background = NightBg,
    surface = NightSurface,
    onBackground = NightText,
    onSurface = NightText,
    primary = GoldAccent,
    onPrimary = NightBg
)

class UrgeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text("Not now.", color = NightText, fontSize = 28.sp,
                        fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    Spacer(Modifier.height(20.dp))
                    Text("Breathe. This urge will pass.", color = NightMuted, fontSize = 15.sp,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(48.dp))
                    Text(reminder.text, color = NightText, fontSize = 15.sp,
                        textAlign = TextAlign.Center, lineHeight = 24.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(reminder.source, color = NightMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(56.dp))
                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                    ) { Text("I am still struggling", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            log.record("resisted")
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
                    ) { Text("The urge is fading", fontSize = 14.sp) }
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = { finish() }) { Text("Close", color = NightMuted, fontSize = 12.sp) }
                }
                1 -> {
                    Text("Do something else.", color = NightText, fontSize = 24.sp,
                        fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(24.dp))
                    Text("Stand up. Drink water. Change rooms.\nMake wudu. Step outside.", color = NightMuted,
                        fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 26.sp)
                    Spacer(Modifier.height(40.dp))
                    Text(reminder.text, color = NightText, fontSize = 14.sp,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(reminder.source, color = NightMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(40.dp))
                    Button(
                        onClick = {
                            log.record("resisted")
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                    ) { Text("I got through it", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            log.record("slipped")
                            step = 3
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
                    ) { Text("I slipped", fontSize = 14.sp) }
                }
                2 -> {
                    Text("Alhamdulillah.", color = NightText, fontSize = 24.sp,
                        fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(16.dp))
                    Text("That moment was a choice. You made it.", color = NightMuted,
                        fontSize = 15.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(48.dp))
                    Button(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                    ) { Text("Return", fontSize = 15.sp) }
                }
                3 -> {
                    Text("Return to Allah.", color = NightText, fontSize = 24.sp,
                        fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Text("Say Astaghfirullah, intend not to repeat, and do not despair.",
                        color = NightMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(36.dp))
                    Text(reminder.text, color = NightText, fontSize = 14.sp,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(reminder.source, color = NightMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(40.dp))
                    Button(
                        onClick = {
                            log.record("tawbah")
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                    ) { Text("I have repented", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = { finish() }) { Text("Close", color = NightMuted, fontSize = 12.sp) }
                }
            }
        }
    }
}
