package com.jannahos

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                BackHandler {
                    val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                        addCategory(android.content.Intent.CATEGORY_HOME)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                }
                var secondsLeft by remember { mutableIntStateOf(60) }
                val dua = remember { listOf(
                    "\"And when My servants ask you concerning Me - indeed I am near.\" (2:186)",
                    "\"Verily, in the remembrance of Allah do hearts find rest.\" (13:28)",
                    "\"So remember Me; I will remember you.\" (2:152)"
                ).random() }
                LaunchedEffect(Unit) {
                    while (secondsLeft > 0) {
                        delay(1000)
                        secondsLeft--
                    }
                    finish()
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent)
                    }
                }

                Column(
                    Modifier.fillMaxSize().background(JannahColors.Cream).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("BLOCKED", style = com.jannahos.ui.theme.JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(8.dp))
                    Text("Focus mode is active.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(dua, fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 18.sp, color = JannahColors.OrangeDeep, textAlign = TextAlign.Center, lineHeight = 28.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("Take one minute to breathe and remember your intention.", style = com.jannahos.ui.theme.JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
                    Spacer(Modifier.height(48.dp))
                    
                    if (secondsLeft > 0) {
                        Text("${secondsLeft}s", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(50))
                                .background(JannahColors.Orange)
                                .clickable {
                                    finish()
                                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(launchIntent)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Return to Jannah", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
