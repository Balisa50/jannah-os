package com.jannahos.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
fun CharacterScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("character_prefs", Context.MODE_PRIVATE) }
    
    var kindness by remember { mutableStateOf(prefs.getBoolean("kindness", false)) }
    var restraint by remember { mutableStateOf(prefs.getBoolean("restraint", false)) }
    var connection by remember { mutableStateOf(prefs.getBoolean("connection", false)) }
    var family by remember { mutableStateOf(prefs.getBoolean("family", false)) }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Character", onBack = onBack)
        Column(Modifier.padding(horizontal = JannahSpacing.gutter).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(JannahSpacing.m))
            
            CharItem("Kindness", "Did you do one small kindness without being seen?", kindness) { kindness = it; prefs.edit().putBoolean("kindness", it).apply() }
            CharItem("Restraint", "Did you lower your gaze / your tongue where you should have?", restraint) { restraint = it; prefs.edit().putBoolean("restraint", it).apply() }
            CharItem("Connection", "Did you make dua or read Quran outside of the obligatory?", connection) { connection = it; prefs.edit().putBoolean("connection", it).apply() }
            CharItem("Family", "Did you honour a parent, sibling, spouse, or child today?", family) { family = it; prefs.edit().putBoolean("family", it).apply() }
            
            Spacer(Modifier.height(JannahSpacing.xl))
            Box(Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                Column {
                    Text("WHY THIS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(4.dp))
                    Text("The Prophet ﷺ said: \"I have only been sent to perfect good character.\" (Ahmad 8952, graded sahih by al-Albani). Character is not separate from worship, it is the fruit of it.", style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 22.sp)
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun CharItem(title: String, desc: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().pressClickable { onToggle(!checked) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(if (checked) JannahColors.Orange else JannahColors.Card).pressClickable { onToggle(!checked) }) {
            if (checked) Text("✓", color = JannahColors.OnOrange, modifier = Modifier.align(Alignment.Center), fontSize = 12.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(desc, style = JannahType.caption, color = JannahColors.Muted)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
}
