package com.jannahos

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

class SalahCommitmentActivity : ComponentActivity() {
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        LockState.restore(this)

        if (!LockState.isEnforcing()) {
            finishAndRemoveTask()
            return
        }

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        DndHelper.enableDnd(this)

        // Start vibration ONLY when the lock is actually active
        startVibration()

        val lat = Settings.lat(this)
        val lon = Settings.lon(this)
        val times = SalahScheduler.todayTimes(this, lat, lon)
        val now = System.currentTimeMillis()
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        
        var current = "fajr"
        for (key in ordered) {
            if ((times[key] ?: 0L) <= now) current = key
        }
        val currentPrayer = current

        setContent {
            JannahTheme {
                LockScreen(prayer = currentPrayer, onFinish = { 
                    stopVibration()
                    completeLock() 
                }, onEmergencyBypass = {
                    stopVibration()
                    completeLock(bypassed = true)
                })
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
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means repeat
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    override fun onStart() { super.onStart(); LockState.isLockActive = true }
    override fun onStop() { LockState.isLockActive = false; super.onStop() }
    override fun onDestroy() { stopVibration(); DndHelper.disableDnd(this); super.onDestroy() }

    private fun completeLock(bypassed: Boolean = false) {
        LockState.release(this)
        finishAndRemoveTask()
        overridePendingTransition(0, 0)
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, SalahCommitmentActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}

@Composable
private fun LockScreen(prayer: String, onFinish: () -> Unit, onEmergencyBypass: () -> Unit) {
    var remaining by remember { mutableLongStateOf(LockState.remainingMs()) }
    var tapCount by remember { mutableIntStateOf(0) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        while (remaining > 0L) {
            delay(250)
            remaining = LockState.remainingMs()
        }
    }

    BackHandler(enabled = remaining > 0L) { }

    Column(
        Modifier.fillMaxSize().background(JannahColors.Cream).padding(JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (remaining > 0L) {
            Text("PRAYER LOCK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(16.dp))
            Text("${SalahTracker.label(prayer)} time.", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text("Put the phone down. Go pray.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 20.sp, color = JannahColors.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            
            val s = (remaining + 999) / 1000
            Text("%02d:%02d".format(s / 60, s % 60), fontFamily = FontFamily.Serif, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange, modifier = Modifier.pressClickable {
                tapCount++
                if (tapCount >= 5) {
                    LockState.release(ctx)
                    onFinish()
                }
            })

            Spacer(Modifier.height(48.dp))
            
            // EMERGENCY BYPASS BUTTON
            Text("Emergency Pass", style = JannahType.caption, color = JannahColors.Danger, modifier = Modifier.pressClickable { showEmergencyDialog = true }.padding(8.dp))
            
        } else {
            Text("Prayer time complete.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text("May Allah accept your prayer.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { onFinish() },
                contentAlignment = Alignment.Center
            ) {
                Text("Finish", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            containerColor = JannahColors.Surface,
            title = { Text("Emergency Bypass?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = JannahColors.Ink) },
            text = { Text("This will unlock the phone. It will also deduct 5 points from your Focus Score. Use only for genuine emergencies.", style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = { showEmergencyDialog = false; onEmergencyBypass() }) { Text("Unlock", color = JannahColors.Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) { Text("Cancel", color = JannahColors.Muted) }
            }
        )
    }
}
