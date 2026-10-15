package com.jannahos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NightBg = Color(0xFF0E1116)
private val NightSurface = Color(0xFF161A22)
private val NightText = Color(0xFFE8E5DC)
private val NightMuted = Color(0xFF8B8F98)
private val GoldAccent = Color(0xFFC9A961)

private val JannahColors = darkColorScheme(
    background = NightBg,
    surface = NightSurface,
    onBackground = NightText,
    onSurface = NightText,
    primary = GoldAccent,
    onPrimary = NightBg
)

class MainActivity : ComponentActivity() {

    private val lat = 13.4549
    private val lon = -16.5790

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.RECORD_AUDIO
                ), 100
            )
        }
        setContent {
            MaterialTheme(colorScheme = JannahColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
                    Dashboard()
                }
            }
        }
    }

    @Composable
    private fun Dashboard() {
        val refresh = remember { mutableStateOf(0) }
        val ignore = refresh.value

        val log = WakeLog(this)
        val history = log.lastNights(7)
        val common = log.commonReason()

        val next = TahajjudScheduler.computeTonight(lat, lon)
        val scheduled = TahajjudScheduler.nextScheduled(this)
        val fmt = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Tonight", color = NightText, fontSize = 30.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(28.dp))

            Text("Last third begins", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text(fmt.format(next), color = NightText, fontSize = 20.sp)

            Spacer(Modifier.height(28.dp))

            if (common != null) {
                Text("Recent pattern", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text("You most often miss because: " + readableReason(common),
                    color = NightText, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
            }

            Button(
                onClick = {
                    TahajjudScheduler.schedule(this@MainActivity, next)
                    refresh.value = refresh.value + 1
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
            ) {
                Text("Set tahajjud alarm", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    TahajjudScheduler.cancel(this@MainActivity)
                    refresh.value = refresh.value + 1
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Cancel alarm", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (scheduled > 0L) "Alarm set: " + fmt.format(Date(scheduled)) else "No alarm set",
                color = NightMuted, fontSize = 12.sp, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            if (history.isNotEmpty()) {
                Text("Last 7 nights", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                history.forEach { e ->
                    val day = dayFmt.format(Date(e.ts))
                    val status = when (e.outcome) {
                        "prayed" -> "Prayed"
                        "missed" -> "Missed"
                        else -> e.outcome
                    }
                    val statusColor = if (e.outcome == "prayed") GoldAccent else NightMuted

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day, color = NightMuted, fontSize = 13.sp)
                        Text(status, color = statusColor, fontSize = 13.sp)
                    }
                }
            } else {
                Text("No nights recorded yet.", color = NightMuted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(40.dp))

            TextButton(onClick = {
                startActivity(Intent(this@MainActivity, TahajjudAlarmActivity::class.java))
            }) {
                Text("Preview wake flow", color = NightMuted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    private fun readableReason(code: String): String = when (code) {
        "not_now" -> "you tapped \"Not now\""
        "slept_late" -> "you slept too late"
        "alarm_missed" -> "the alarm did not wake you"
        "phone_far" -> "the phone was too far"
        "went_back" -> "you went back to sleep"
        else -> code
    }
}
