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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val NightBg = Color(0xFF0E1116)
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

class BedtimeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
                    BedtimeScreen()
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Tonight's plan", color = NightText, fontSize = 26.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(40.dp))

            Text("I intend to sleep at", color = NightMuted, fontSize = 13.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            Text(fmt.format(hourMinToDate(hour, minute)), color = NightText, fontSize = 34.sp,
                fontWeight = FontWeight.Light)

            Spacer(Modifier.height(24.dp))

            Row(
                leftLabel = "− 15m",
                onLeft = {
                    var total = hour * 60 + minute - 15
                    if (total < 0) total += 24 * 60
                    hour = (total / 60) % 24
                    minute = total % 60
                },
                rightLabel = "+ 15m",
                onRight = {
                    var total = hour * 60 + minute + 15
                    total %= 24 * 60
                    hour = total / 60
                    minute = total % 60
                }
            )

            Spacer(Modifier.height(40.dp))

            Text("A reminder", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Text("Surah Al-Mulk before sleep", color = NightText, fontSize = 16.sp,
                fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text("\"Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.\" (Reported in Jami' at-Tirmidhi 2891; graded hasan by some scholars, da'if by others — differences exist.)",
                color = NightMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    BedtimeStore.set(this@BedtimeActivity, hour, minute)
                    finish()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
            ) {
                Text("Save and return", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { finish() }) {
                Text("Cancel", color = NightMuted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    @Composable
    private fun Row(
        leftLabel: String,
        onLeft: () -> Unit,
        rightLabel: String,
        onRight: () -> Unit
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLeft,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) { Text(leftLabel, fontSize = 14.sp) }
            Button(
                onClick = onRight,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
            ) { Text(rightLabel, fontSize = 14.sp) }
        }
    }

    private fun hourMinToDate(h: Int, m: Int): java.util.Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, h)
        c.set(Calendar.MINUTE, m)
        c.set(Calendar.SECOND, 0)
        return c.time
    }
}
