package com.jannahos

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

// Calm, low-light palette. Deep indigo ground, muted gold accent.
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
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        setContent {
            MaterialTheme(colorScheme = JannahColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TonightScreen()
                }
            }
        }
    }

    @Composable
    private fun TonightScreen() {
        val refresh = remember { mutableStateOf(0) }
        val next = TahajjudScheduler.computeTonight(lat, lon)
        val scheduled = TahajjudScheduler.nextScheduled(this)
        val fmt = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())

        // Force recompose when refresh changes.
        val ignore = refresh.value

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tonight",
                color = NightText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Suggested tahajjud",
                color = NightMuted,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = fmt.format(next),
                color = NightText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = {
                    TahajjudScheduler.schedule(this@MainActivity, next)
                    refresh.value = refresh.value + 1
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = NightBg
                )
            ) {
                Text("Set tahajjud alarm", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    TahajjudScheduler.cancel(this@MainActivity)
                    refresh.value = refresh.value + 1
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NightSurface,
                    contentColor = NightText
                )
            ) {
                Text("Cancel alarm", fontSize = 15.sp)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = if (scheduled > 0L) "Alarm set: " + fmt.format(Date(scheduled)) else "No alarm set",
                color = NightMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
