package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class TahajjudAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Column(
                    Modifier.fillMaxSize().background(JannahColors.Cream).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Tahajjud", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(16.dp))
                    Text("It is the last third of the night. Stand up and pray.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Box(
                        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).clickable { finish() },
                        contentAlignment = Alignment.Center
                    ) { Text("I am awake", style = JannahType.button, color = JannahColors.OnOrange) }
                }
            }
        }
    }
}
