package com.jannahos.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

@Composable
fun PrayerFocusScreen(prayerName: String, onDone: () -> Unit) {
    val ctx = LocalContext.current
    var isFaceDown by remember { mutableStateOf(false) }
    var secondsPassed by remember { mutableIntStateOf(0) }
    var isDone by remember { mutableStateOf(false) }
    var showSunnahPrompt by remember { mutableStateOf(false) }

    val tafsirVerses = listOf(
        "\"Indeed, prayer prohibits immorality and wrongdoing.\" (Qur'an 29:45)",
        "\"And establish prayer and give zakah and bow with those who bow.\" (Qur'an 2:43)",
        "\"Successful indeed are the believers, who are humble in their prayers.\" (Qur'an 23:1-2)",
        "\"Guard strictly the prayers, especially the middle prayer.\" (Qur'an 2:238)"
    )
    val randomTafsir = remember { tafsirVerses.random() }

    // 1. SILENCE THE PHONE (DND)
    DisposableEffect(Unit) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
        onDispose {
            if (nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    // 2. FACE DOWN SENSOR
    DisposableEffect(Unit) {
        val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // If Z axis is positive (facing down), the phone is face down.
                isFaceDown = event.values[2] > 9.0f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // 3. TIMER (10 Minutes = 600 Seconds)
    LaunchedEffect(Unit) {
        while (secondsPassed < 600) {
            delay(1000)
            // Only count time if the phone is face down
            if (isFaceDown) {
                secondsPassed++
            }
        }
        isDone = true
    }

    val remaining = 600 - secondsPassed
    val mm = remaining / 60
    val ss = remaining % 60

    Column(
        Modifier
            .fillMaxSize()
            .background(if (isFaceDown) JannahColors.Ink else JannahColors.Cream)
            .padding(JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isDone) {
            Text("Prayer Complete", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (isFaceDown) JannahColors.Cream else JannahColors.Ink)
            Spacer(Modifier.height(16.dp))
            Text("May Allah accept it from you.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange)
            
            Spacer(Modifier.height(24.dp))
            
            // SUNNAH STACK
            if (!showSunnahPrompt) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFaceDown) JannahColors.Cream.copy(alpha=0.1f) else JannahColors.Card)
                        .clickable { showSunnahPrompt = true }
                        .padding(16.dp)
                ) {
                    Text("Did you pray the Sunnah Rak'ahs?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = if (isFaceDown) JannahColors.Cream else JannahColors.Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Text("The Prophet ﷺ said: \"Whoever prays 12 rak'ahs in a day and night will have a house built for him in Paradise.\"", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = if (isFaceDown) JannahColors.Cream.copy(alpha=0.8f) else JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 22.sp)
            }
            
            Spacer(Modifier.height(32.dp))
            
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(JannahColors.Orange)
                    .clickable { onDone() },
                contentAlignment = Alignment.Center
            ) {
                Text("Return to Jannah", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Text("$prayerName Focus", style = JannahType.eyebrow, color = JannahColors.Orange)
            Spacer(Modifier.height(16.dp))
            
            Text(String.format("%02d:%02d", mm, ss), fontFamily = FontFamily.Serif, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = if (isFaceDown) JannahColors.Cream else JannahColors.Ink)
            
            Spacer(Modifier.height(32.dp))
            
            if (!isFaceDown) {
                Text("Place your phone face down on the table to begin.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text("The timer will pause if you pick it up.", style = JannahType.caption, color = JannahColors.OrangeDeep)
            } else {
                Text("Focus mode active. Do not pick up the phone.", style = JannahType.body, color = JannahColors.Cream.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(48.dp))

            // MICRO-TAFSIR
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    randomTafsir,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = if (isFaceDown) JannahColors.Cream.copy(alpha = 0.6f) else JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
