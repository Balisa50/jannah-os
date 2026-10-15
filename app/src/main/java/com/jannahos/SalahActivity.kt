package com.jannahos

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

class SalahActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
                    SalahScreen()
                }
            }
        }
    }

    @Composable
    private fun SalahScreen() {
        var refresh by remember { mutableStateOf(0) }
        val ignore = refresh
        val today = SalahTracker.today(this)
        val done = today.count { it.value }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Today's prayers", color = NightText, fontSize = 26.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(16.dp))

            Text("$done of 5", color = NightMuted, fontSize = 14.sp, letterSpacing = 1.sp)

            Spacer(Modifier.height(40.dp))

            today.forEach { (key, completed) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(SalahTracker.label(key), color = NightText, fontSize = 16.sp)
                    Button(
                        onClick = {
                            SalahTracker.toggle(this@SalahActivity, key)
                            refresh = refresh + 1
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (completed) GoldAccent else NightSurface,
                            contentColor = if (completed) NightBg else NightText
                        )
                    ) {
                        Text(if (completed) "Prayed" else "Mark", fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "A private record. No streak resets. No punishment. Return, always.",
                color = NightMuted, fontSize = 12.sp,
                textAlign = TextAlign.Center, lineHeight = 18.sp
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
