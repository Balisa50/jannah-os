package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BedtimeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                SwipeBackScreen(onBack = { finish() }) { BedtimeScreen() }
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)
        ) {
            TopBar("Tonight's plan", onBack = { finish() })

            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Spacer(Modifier.height(JannahSpacing.l))
                
                Text("I INTEND TO SLEEP AT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                
                Text(fmt.format(hourMinToDate(hour, minute)),
                    fontFamily = FontFamily.Serif, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)

                Spacer(Modifier.height(JannahSpacing.m))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable {
                            var total = hour * 60 + minute - 15
                            if (total < 0) total += 24 * 60
                            hour = (total / 60) % 24
                            minute = total % 60
                        },
                        contentAlignment = Alignment.Center
                    ) { Text("− 15m", style = JannahType.body, color = JannahColors.Ink) }

                    Box(
                        Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable {
                            var total = hour * 60 + minute + 15
                            total %= 24 * 60
                            hour = total / 60
                            minute = total % 60
                        },
                        contentAlignment = Alignment.Center
                    ) { Text("+ 15m", style = JannahType.body, color = JannahColors.Ink) }
                }

                Spacer(Modifier.height(JannahSpacing.xl))
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                Spacer(Modifier.height(JannahSpacing.xl))

                Text("A REMINDER", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Text("Surah Al-Mulk before sleep", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    "\"Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.\" (Jami' at-Tirmidhi 2891)",
                    style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp
                )

                Spacer(Modifier.height(JannahSpacing.xl))

                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable {
                        BedtimeStore.set(this@BedtimeActivity, hour, minute)
                        BedtimeScheduler.schedule(this@BedtimeActivity)
                        finish()
                    },
                    contentAlignment = Alignment.Center
                ) { Text("Save Plan", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold) }

                Spacer(Modifier.height(JannahSpacing.xxl))
            }
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
