package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

class SalahCommitmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prayer = intent.getStringExtra("prayer") ?: "fajr"
        
        setContent {
            JannahTheme {
                PrayerLockContent(prayer = prayer, onFinish = { finishAndRemoveTask() })
            }
        }
    }
}

@Composable
fun PrayerLockContent(prayer: String, onFinish: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    BackHandler { /* Cannot go back */ }
    
    val durationMinutes = PrayerLock.durationMinutes(ctx)
    var remaining by remember { mutableLongStateOf(durationMinutes * 60_000L) }
    var isDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (remaining > 0L) {
            delay(1000)
            remaining -= 1000L
        }
        isDone = true
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()

    Column(
        Modifier.fillMaxSize().background(JannahColors.Cream).padding(JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isDone) {
            Text("Prayer time complete.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text("May Allah accept your prayer.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { onFinish() },
                contentAlignment = Alignment.Center
            ) {
                Text("Finish", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("PRAYER LOCK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(16.dp))
            Text("${SalahTracker.label(prayer)} time.", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text("Put the phone down. Go pray.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 20.sp, color = JannahColors.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Text(String.format("%02d:%02d", mm, ss), fontFamily = FontFamily.Serif, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
        }
    }
}
