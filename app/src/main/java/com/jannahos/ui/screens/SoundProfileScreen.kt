package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.jannahos.Settings
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun SoundProfileScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    var selected by remember { mutableStateOf("Makkah") }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Sound Profiles", eyebrow = "AUDIO", subtitle = "Choose your Adhan.", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            
            listOf("Makkah", "Madinah", "Al-Aqsa", "Silent").forEach { profile ->
                Column {
                    Row(Modifier.fillMaxWidth().clickable { selected = profile }.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(profile, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = if (selected == profile) FontWeight.Bold else FontWeight.Normal, color = if (selected == profile) JannahColors.Orange else JannahColors.Ink, modifier = Modifier.weight(1f))
                        Box(Modifier.size(24.dp).clip(CircleShape).background(if (selected == profile) JannahColors.Orange else JannahColors.Border))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }
            Spacer(Modifier.height(JannahSpacing.l))
            Text("Audio files are downloaded on-demand to save storage.", style = JannahType.caption, color = JannahColors.Muted)
        }
    }
}
