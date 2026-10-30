package com.jannahos

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class PrayerAlarmActivity : ComponentActivity() {
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prayer = intent.getStringExtra("prayer") ?: "Fajr"
        startVibration()

        setContent {
            JannahTheme {
                var tapCount by remember { mutableIntStateOf(0) }
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF141210)).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("PRAYER ALARM", style = JannahType.eyebrow, color = JannahColors.Orange)
                    Spacer(Modifier.height(16.dp))
                    Text(prayer, fontFamily = FontFamily.Serif, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF3EFE6))
                    Spacer(Modifier.height(24.dp))
                    Text("“Verily, the prayer is enjoined on the believers at fixed hours.”", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("Qur'an 4:103", style = JannahType.caption, color = Color.White.copy(alpha = 0.4f))
                    Spacer(Modifier.height(64.dp))
                    Box(
                        Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(50))
                            .background(JannahColors.Orange)
                            .pressClickable { stopVibration(); finishAndRemoveTask() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("I am awake", style = JannahType.button, color = JannahColors.OnOrange, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Dismiss", style = JannahType.caption, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.pressClickable {
                        tapCount++
                        if (tapCount >= 5) { stopVibration(); finishAndRemoveTask() }
                    }.padding(16.dp))
                }
            }
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 1)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    override fun onDestroy() {
        stopVibration()
        super.onDestroy()
    }
}
