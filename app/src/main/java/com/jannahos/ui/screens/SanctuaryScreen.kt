package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.jannahos.DndHelper
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

@Composable
fun SanctuaryScreen(onExit: () -> Unit) {
    val ctx = LocalContext.current
    var selectedMinutes by remember { mutableStateOf<Int?>(null) }
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    var tapCount by remember { mutableIntStateOf(0) }
    var isActive by remember { mutableStateOf(false) }

    // DND Control
    LaunchedEffect(isActive) {
        if (isActive) {
            DndHelper.enableDnd(ctx)
        } else {
            DndHelper.disableDnd(ctx)
        }
    }

    // Timer Control
    LaunchedEffect(isActive, selectedMinutes) {
        if (isActive && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            isActive = false // Timer finished
        }
    }

    BackHandler(enabled = isActive) { /* Force them to tap 5 times or wait */ }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream).padding(24.dp)) {
        if (!isActive) {
            // Selection Screen
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("SIRAJ · FOCUS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(10.dp))
                Text("Sanctuary", fontFamily = FontFamily.Serif, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(8.dp))
                Text("Make room for what matters. Do Not Disturb activates when you begin.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = JannahColors.OrangeDeep, textAlign = TextAlign.Center, lineHeight = 25.sp)
                
                Spacer(Modifier.height(48.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(15, 25, 45).forEach { mins ->
                        Box(
                            Modifier.size(80.dp).clip(CircleShape)
                                .background(if (selectedMinutes == mins) JannahColors.Orange else JannahColors.Card)
                                .pressClickable { selectedMinutes = mins },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$mins", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (selectedMinutes == mins) JannahColors.OnOrange else JannahColors.Ink)
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))

                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50))
                        .background(if (selectedMinutes != null) JannahColors.Ink else JannahColors.Border)
                        .pressClickable(enabled = selectedMinutes != null) {
                            remainingSeconds = (selectedMinutes ?: 0) * 60L
                            isActive = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Begin", style = JannahType.button, color = JannahColors.OnOrange)
                }

                Spacer(Modifier.height(24.dp))
                
                Text("Cancel", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.pressClickable { onExit() }.padding(8.dp))
            }
        } else {
            // Active Timer Screen
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("SIRAJ · SANCTUARY ACTIVE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(24.dp))
                
                val mm = remainingSeconds / 60
                val ss = remainingSeconds % 60
                
                Text(
                    String.format(java.util.Locale.US, "%02d:%02d", mm, ss),
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    modifier = Modifier.pressClickable {
                        tapCount++
                        if (tapCount >= 5) {
                            isActive = false
                            onExit()
                        }
                    }
                )
                
                Spacer(Modifier.height(48.dp))
                
                Text("Put the phone down.\nBe present.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 20.sp, color = JannahColors.Muted, textAlign = TextAlign.Center, lineHeight = 28.sp)
            }
        }
    }
}
