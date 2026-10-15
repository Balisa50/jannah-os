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

private val REASONS = listOf(
    "slept_late" to "I slept too late",
    "alarm_missed" to "The alarm did not wake me",
    "phone_far" to "The phone was too far",
    "went_back" to "I woke but went back",
    "too_tired" to "I was too exhausted"
)

private fun readableReason(code: String): String =
    REASONS.firstOrNull { it.first == code }?.second ?: code

class MainActivity : ComponentActivity() {

    private val lat: Double get() = Settings.lat(this)
    private val lon: Double get() = Settings.lon(this)

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
                    val pending = remember { WakeLog(this).pendingFollowUp() }
                    if (pending != null) {
                        FollowUpScreen(pending)
                    } else {
                        Dashboard()
                    }
                }
            }
        }
    }

    @Composable
    private fun FollowUpScreen(event: WakeLog.Event) {
        val log = remember { WakeLog(this) }
        val answered = remember { mutableStateOf(false) }
        val dayFmt = SimpleDateFormat("EEEE", Locale.getDefault())

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!answered.value) {
                Text("Last night", color = NightMuted, fontSize = 13.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                Text("What got in the way?", color = NightText, fontSize = 24.sp,
                    fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text("This shapes tonight's plan. It is not a punishment.",
                    color = NightMuted, fontSize = 13.sp, textAlign = TextAlign.Center)

                Spacer(Modifier.height(40.dp))

                REASONS.forEach { (code, label) ->
                    Button(
                        onClick = {
                            log.setReason(event.id, code)
                            answered.value = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NightSurface,
                            contentColor = NightText
                        )
                    ) {
                        Text(label, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                TextButton(onClick = { answered.value = true }) {
                    Text("Skip", color = NightMuted, fontSize = 12.sp)
                }
            } else {
                Text("Recorded.", color = NightText, fontSize = 22.sp,
                    fontWeight = FontWeight.Light)
                Spacer(Modifier.height(12.dp))
                Text("Tonight's plan adjusts.", color = NightMuted, fontSize = 14.sp)
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = { recreate() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                ) {
                    Text("Continue", fontSize = 15.sp)
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
        val prayed = log.prayedCount()

        val next = TahajjudScheduler.computeTonight(lat, lon)
        val scheduled = TahajjudScheduler.nextScheduled(this)
        val fmt = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

        // Adaptive hint: if we know the pattern, adjust the suggestion.
        val adaptiveHint = when (common) {
            "slept_late" -> "Try to sleep earlier tonight."
            "alarm_missed" -> "Consider a louder alarm or a second alarm."
            "phone_far" -> "Keep your phone near your bed."
            "went_back" -> "Plan to sit up for one minute before deciding."
            "too_tired" -> "Consider a nap today, or shorten tonight's plan."
            else -> null
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Tonight", color = NightText, fontSize = 30.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(28.dp))

            Text("Last third begins", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text(fmt.format(next), color = NightText, fontSize = 20.sp)

            if (adaptiveHint != null) {
                Spacer(Modifier.height(20.dp))
                Text(adaptiveHint, color = GoldAccent, fontSize = 13.sp,
                    textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(32.dp))

            // Today's five prayers
            val times = SalahScheduler.todayTimes(lat, lon)
            val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text("Today's prayers", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))

            times.forEach { (key, t) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(SalahTracker.label(key), color = NightText, fontSize = 14.sp)
                    Text(hhmm.format(t), color = NightMuted, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { SalahScheduler.schedule(this@MainActivity, lat, lon) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Set prayer reminders", fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))

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

            Spacer(Modifier.height(36.dp))

            if (prayed > 0) {
                Text("Nights prayed: $prayed", color = NightMuted, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
            }

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
            }

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, SalahActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                val done = SalahTracker.today(this@MainActivity).count { it.value }
                Text("Today's prayers ($done/5)", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, BedtimeActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Tonight's plan", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, IstighfarActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Istighfar", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, QuranActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Quran", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, PlannerActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Planner", fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { startActivity(Intent(this@MainActivity, UrgeActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) {
                Text("Not now", fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = {
                startActivity(Intent(this@MainActivity, TahajjudAlarmActivity::class.java))
            }) {
                Text("Preview wake flow", color = NightMuted, fontSize = 11.sp)
            }

            TextButton(onClick = {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }) {
                Text("Settings", color = NightMuted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
