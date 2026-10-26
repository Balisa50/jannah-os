package com.jannahos

import android.os.Bundle
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
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

class TahajjudAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            var tapCount by remember { mutableIntStateOf(0) }
            
            // Deep dark mode background
            val bgColor = Color(0xFF141210)
            val textColor = Color(0xFFF3EFE6)
            val accentColor = JannahColors.Orange

            Column(
                Modifier.fillMaxSize().background(bgColor).padding(JannahSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("THE NIGHT PRAYER", style = JannahType.eyebrow, color = accentColor.copy(alpha = 0.8f))
                Spacer(Modifier.height(16.dp))
                
                Text("Tahajjud", fontFamily = FontFamily.Serif, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "“Our Lord descends to the lowest heaven in the last third of the night and says: Who is calling upon Me that I may answer him?”",
                    fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = textColor.copy(alpha = 0.7f), textAlign = TextAlign.Center, lineHeight = 28.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("Sahih al-Bukhari 1145", style = JannahType.micro, color = textColor.copy(alpha = 0.4f))
                
                Spacer(Modifier.height(64.dp))
                
                // 5-tap escape hatch on the prompt
                Text(
                    "Pray 2 Rakats.",
                    fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentColor,
                    modifier = Modifier.pressClickable {
                        tapCount++
                        if (tapCount >= 5) {
                            finishAndRemoveTask()
                        }
                    }
                )

                Spacer(Modifier.height(48.dp))

                // Dismiss button
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50))
                        .background(accentColor)
                        .pressClickable { finishAndRemoveTask() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("I'm Awake", style = JannahType.button, color = JannahColors.OnOrange)
                }
            }
        }
    }
}
