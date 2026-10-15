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

class SalahActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Today's prayers", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.m))

            Text("$done of 5", style = JannahType.secondary, color = JannahColors.DayMuted)

            Spacer(Modifier.height(JannahSpacing.xl))

            today.forEach { (key, completed) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(SalahTracker.label(key), style = JannahType.primary, color = JannahColors.DayText)
                    Button(
                        onClick = {
                            SalahTracker.toggle(this@SalahActivity, key)
                            refresh = refresh + 1
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (completed) JannahColors.Accent else JannahColors.DaySurface,
                            contentColor = if (completed) JannahColors.AccentInk else JannahColors.DayText
                        )
                    ) {
                        Text(if (completed) "Prayed" else "Mark", style = JannahType.secondary)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text(
                "A private record. No streak resets. No punishment. Return, always.",
                style = JannahType.tertiary,
                color = JannahColors.DayMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
