package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    
    // Modal state
    var showFormation by remember { mutableStateOf(false) }
    var showWeekly by remember { mutableStateOf(false) }
    var showDiary by remember { mutableStateOf(false) }
    var showStruggle by remember { mutableStateOf(false) }
    var showCompanion by remember { mutableStateOf(false) }
    var showLetters by remember { mutableStateOf(false) }
    var showDuaJournal by remember { mutableStateOf(false) }
    var showFast by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }
    var showBackup by remember { mutableStateOf(false) }
    var showDuas by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showZakat by remember { mutableStateOf(false) }
    var showHifdh by remember { mutableStateOf(false) }
    var showCharacter by remember { mutableStateOf(false) }
    var showSunnah by remember { mutableStateOf(false) }
    var showAdhkar by remember { mutableStateOf(false) }
    var showSadaqah by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showRamadan by remember { mutableStateOf(false) }
    var showFinance by remember { mutableStateOf(false) }
    var showIfThen by remember { mutableStateOf(false) }
    var showFriction by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    // Navigation routing
    if (showFormation) { FormationScreen(onBack = { showFormation = false }); return }
    if (showWeekly) { WeeklyReviewScreen(onBack = { showWeekly = false }); return }
    if (showDiary) { DiaryScreen(onBack = { showDiary = false }); return }
    if (showStruggle) { StruggleScreen(onBack = { showStruggle = false }); return }
    if (showCompanion) { CompanionScreen(onBack = { showCompanion = false }, onNavigate = { r -> showCompanion = false }); return }
    if (showLetters) { LettersScreen(onBack = { showLetters = false }); return }
    if (showDuaJournal) { DuaJournalScreen(onBack = { showDuaJournal = false }); return }
    if (showFast) { FastScreen(onBack = { showFast = false }); return }
    if (showSleep) { SleepScreen(onBack = { showSleep = false }); return }
    if (showBackup) { BackupScreen(onBack = { showBackup = false }); return }
    if (showDuas) { DuaLibraryScreen(onBack = { showDuas = false }); return }
    if (showCalendar) { CalendarScreen(onBack = { showCalendar = false }); return }
    if (showZakat) { ZakatScreen(onBack = { showZakat = false }); return }
    if (showHifdh) { HifdhScreen(onBack = { showHifdh = false }); return }
    if (showCharacter) { CharacterScreen(onBack = { showCharacter = false }); return }
    if (showSunnah) { SunnahScreen(onBack = { showSunnah = false }); return }
    if (showAdhkar) { AdhkarScreen(onBack = { showAdhkar = false }); return }
    if (showSadaqah) { SadaqahScreen(onBack = { showSadaqah = false }); return }
    if (showAbout) { AboutScreen(onBack = { showAbout = false }); return }
    if (showRamadan) { RamadanScreen(onBack = { showRamadan = false }); return }
    if (showFinance) { FinanceScreen(onBack = { showFinance = false }); return }
    if (showIfThen) { IfThenScreen(onBack = { showIfThen = false }); return }
    if (showFriction) { FrictionAuditScreen(onBack = { showFriction = false }); return }

    val name = Settings.displayName(ctx)
    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)
    ) {
        // Hero Header
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.jannahos.R.drawable.dhikr_hero),
                contentDescription = "You",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f))
                )
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.m)) {
                Text(if (name.isBlank()) "You." else "$name.", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.height(2.dp))
                Text("Level $level · $prayedWeek of $target nights this week", style = JannahType.caption, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            // Formation
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).pressClickable { showFormation = true }.padding(JannahSpacing.l)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TAHAJJUD", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(6.dp))
                        Text(when (level) { 1 -> "One night a week"; 2 -> "Two nights a week"; 3 -> "Three nights a week"; else -> "Every night" },
                            fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                    }
                    Text("→", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
            }
            Spacer(Modifier.height(JannahSpacing.l))

            SectionLabel("YOUR PRACTICE")
            ListRow("Weekly review") { showWeekly = true }
            Divider()
            ListRow("Diary") { showDiary = true }
            Divider()
            ListRow("Private commitments") { showStruggle = true }
            Divider()
            ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
            Divider()
            ListRow("Dua journal") { showDuaJournal = true }
            Divider()
            ListRow("Letters to future self") { showLetters = true }
            Divider()
            ListRow("Companion") { showCompanion = true }
            Divider()
            ListRow("If-Then Planner") { showIfThen = true }
            Divider()
            ListRow("Friction Audit") { showFriction = true }
            
            Spacer(Modifier.height(JannahSpacing.l))

            if (showMore) {
                SectionLabel("MORE")
                ListRow("Sunnah prayers") { showSunnah = true }
                Divider()
                ListRow("Adhkar") { showAdhkar = true }
                Divider()
                ListRow("Dua library") { showDuas = true }
                Divider()
                ListRow("Hifdh") { showHifdh = true }
                Divider()
                ListRow("Fasting") { showFast = true }
                Divider()
                ListRow("Sadaqah log") { showSadaqah = true }
                Divider()
                ListRow("Zakat calculator") { showZakat = true }
                Divider()
                ListRow("Finance") { showFinance = true }
                Divider()
                ListRow("Sleep log") { showSleep = true }
                Divider()
                ListRow("Character") { showCharacter = true }
                Divider()
                ListRow("Islamic calendar") { showCalendar = true }
                Spacer(Modifier.height(JannahSpacing.l))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.pillRadius)).pressClickable { showMore = false }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Show less", style = JannahType.caption, color = JannahColors.Muted)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card).pressClickable { showMore = true }, contentAlignment = Alignment.Center) {
                    Text("More tools", style = JannahType.button, color = JannahColors.Ink)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))
            SectionLabel("SETTINGS")
            ListRow("About") { showAbout = true }
            Divider()
            ListRow("Backup & restore") { showBackup = true }
            Divider()
            ListRow("Settings") { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }
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
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = JannahSpacing.gutter, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink, modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
}
