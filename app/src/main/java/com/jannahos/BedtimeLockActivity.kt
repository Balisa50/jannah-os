package com.jannahos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

class BedtimeLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockState.restore(this)
        
        if (!LockState.isEnforcing()) {
            finishAndRemoveTask()
            return
        }

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            JannahTheme {
                BedtimeLockContent(onDone = { 
                    LockState.release(this)
                    BedtimeStore.setLockActive(this, false)
                    finishAndRemoveTask() 
                })
            }
        }
    }

    override fun onStart() { super.onStart(); LockState.isLockActive = true }
    override fun onStop() { LockState.isLockActive = false; super.onStop() }

    companion object {
        fun intent(context: Context): Intent = Intent(context, BedtimeLockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}

@Composable
fun BedtimeLockContent(onDone: () -> Unit) {
    var remaining by remember { mutableLongStateOf(LockState.remainingMs()) }
    
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
            Text("BEDTIME LOCK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(16.dp))
            Text("Surah Al-Mulk", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(8.dp))
            Text("The Prophet ﷺ said: \"Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.\"", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(32.dp))
            val s = (remaining + 999) / 1000
            Text("%02d:%02d".format(s / 60, s % 60), fontFamily = FontFamily.Serif, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
        } else {
            Text("Bedtime complete.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text("May Allah protect you through the night.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { onDone() },
                contentAlignment = Alignment.Center
            ) {
                Text("Finish", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}
