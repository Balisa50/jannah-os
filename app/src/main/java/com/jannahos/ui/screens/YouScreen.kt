package com.jannahos.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.*
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    // Navigation state
    var showDiary by remember { mutableStateOf(false) }
    var showStruggle by remember { mutableStateOf(false) }
    var showLetters by remember { mutableStateOf(false) }
    var showWeekly by remember { mutableStateOf(false) }
    var showBackup by remember { mutableStateOf(false) }

    if (showDiary) { DiaryScreen(onBack = { showDiary = false }); return }
    if (showStruggle) { StruggleScreen(onBack = { showStruggle = false }); return }
    if (showLetters) { LettersScreen(onBack = { showLetters = false }); return }
    if (showWeekly) { WeeklyReviewScreen(onBack = { showWeekly = false }); return }
    if (showBackup) { BackupScreen(onBack = { showBackup = false }); return }

    val name = Settings.displayName(ctx)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.jannahos.R.drawable.dhikr_hero),
                contentDescription = "You",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.m)) {
                Text(if (name.isBlank()) "You." else "$name.", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))
        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            
            SectionLabel("YOUR PRACTICE")
            ListRow("Weekly review") { showWeekly = true }
            Divider()
            ListRow("Diary") { showDiary = true }
            Divider()
            ListRow("Private commitments") { showStruggle = true }
            Divider()
            ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
            Divider()
            ListRow("Letters to future self") { showLetters = true }
            Divider()
            ListRow("Backup & restore") { showBackup = true }
            
            Spacer(Modifier.height(JannahSpacing.l))
            
            SectionLabel("SETTINGS")
            ListRow("App Settings") { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text, style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(top = JannahSpacing.m, bottom = JannahSpacing.s))
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().pressClickable(onClick = onClick).padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink, modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
}
