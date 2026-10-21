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
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.*

class UrgeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                UrgeFlow()
            }
        }
    }

    @Composable
    private fun UrgeFlow() {
        var step by remember { mutableStateOf(0) }
        val log = remember { StruggleLog(this@UrgeActivity) }
        val reminder = remember { Mercy.random() }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
            TopBar("Resist", onBack = { finish() })
            
            Column(
                Modifier.padding(horizontal = JannahSpacing.gutter).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(JannahSpacing.xl))
                
                when (step) {
                    0 -> {
                        Text("Not now.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("Breathe. This urge will pass.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                            Column {
                                Text(reminder.text, fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 18.sp, color = JannahColors.OrangeDeep, lineHeight = 26.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(reminder.source, style = JannahType.micro, color = JannahColors.Muted)
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Ink).pressClickable { step = 1 }, contentAlignment = Alignment.Center) {
                            Text("I am still struggling", style = JannahType.button, color = JannahColors.OnOrange)
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable { log.record("resisted"); step = 2 }, contentAlignment = Alignment.Center) {
                            Text("The urge is fading", style = JannahType.button, color = JannahColors.Ink)
                        }
                    }
                    1 -> {
                        Text("Do something else.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("Stand up. Drink water. Change rooms.\nMake wudu. Step outside.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
                        
                        Spacer(Modifier.height(32.dp))
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable { log.record("resisted"); step = 2 }, contentAlignment = Alignment.Center) {
                            Text("I got through it", style = JannahType.button, color = JannahColors.OnOrange)
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable { log.record("slipped"); step = 3 }, contentAlignment = Alignment.Center) {
                            Text("I slipped", style = JannahType.button, color = JannahColors.Muted)
                        }
                    }
                    2 -> {
                        Text("Alhamdulillah.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("That moment was a choice. You made it.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable { finish() }, contentAlignment = Alignment.Center) {
                            Text("Return", style = JannahType.button, color = JannahColors.OnOrange)
                        }
                    }
                    3 -> {
                        Text("Return to Allah.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("Say Astaghfirullah, intend not to repeat, and do not despair.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
                        
                        Spacer(Modifier.height(32.dp))
                        Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable { log.record("tawbah"); finish() }, contentAlignment = Alignment.Center) {
                            Text("I have repented", style = JannahType.button, color = JannahColors.OnOrange)
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.xxl))
            }
        }
    }
}
