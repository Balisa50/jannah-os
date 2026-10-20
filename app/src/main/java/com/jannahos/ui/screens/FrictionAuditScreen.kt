package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun FrictionAuditScreen(onBack: () -> Unit) {
    var matVisible by remember { mutableStateOf(false) }
    var phoneAway by remember { mutableStateOf(false) }
    var appOnHome by remember { mutableStateOf(false) }
    
    val score = listOf(matVisible, phoneAway, appOnHome).count { it }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Friction Audit", onBack = onBack)
        
        Column(Modifier.padding(JannahSpacing.gutter)) {
            Text("Environment beats willpower. Reduce friction for the things you want to do, and increase it for the things you don't.", style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp)
            Spacer(Modifier.height(JannahSpacing.xl))
            
            Text("FRICTION SCORE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("$score / 3", fontFamily = FontFamily.Serif, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if (score == 3) JannahColors.Orange else JannahColors.Ink)
            
            Spacer(Modifier.height(JannahSpacing.xl))
            
            AuditItem("Prayer mat is visible on the floor", matVisible) { matVisible = it }
            AuditItem("Phone is in another room during Quran time", phoneAway) { phoneAway = it }
            AuditItem("Quran app is on the home screen", appOnHome) { appOnHome = it }
            
            Spacer(Modifier.height(JannahSpacing.xl))
            
            if (score < 3) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                    Text(
                        when {
                            !matVisible -> "Tip: Leave your prayer mat unrolled. Seeing it cues your brain to pray."
                            !phoneAway -> "Tip: Put your phone in a drawer when you read Quran. Out of sight, out of distraction."
                            else -> "Tip: Put the Jannah widget on your home screen so prayer is the first thing you see."
                        },
                        style = JannahType.bodySmall, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Orange).padding(16.dp)) {
                    Text("Your environment is perfectly optimized. You have removed all friction for worship.", style = JannahType.bodySmall, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AuditItem(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().pressClickable { onToggle(!checked) }.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp).clip(RoundedCornerShape(50)).background(if (checked) JannahColors.Orange else JannahColors.Card).pressClickable { onToggle(!checked) }) {
            if (checked) Text("✓", color = JannahColors.OnOrange, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(16.dp))
        Text(label, style = JannahType.body, color = JannahColors.Ink)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
}
