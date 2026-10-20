package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.pressClickable
import com.jannahos.BedtimeActivity
import com.jannahos.SafeStore
import com.jannahos.Settings
import com.jannahos.SettingsActivity
import com.jannahos.TahajjudPlan
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    // Modal state
    val showFormation = remember { mutableStateOf(false) }
    val showWeekly = remember { mutableStateOf(false) }
    val showDiary = remember { mutableStateOf(false) }
    val showStruggle = remember { mutableStateOf(false) }
    val showCompanion = remember { mutableStateOf(false) }
    val showLetters = remember { mutableStateOf(false) }
    val showDuaJournal = remember { mutableStateOf(false) }

    // Additional
    val showFast = remember { mutableStateOf(false) }
    val showSleep = remember { mutableStateOf(false) }
    val showBackup = remember { mutableStateOf(false) }
    val showDuas = remember { mutableStateOf(false) }
    val showCalendar = remember { mutableStateOf(false) }
    val showZakat = remember { mutableStateOf(false) }
    val showHifdh = remember { mutableStateOf(false) }
    val showCharacter = remember { mutableStateOf(false) }
    val showSunnah = remember { mutableStateOf(false) }
    val showAdhkar = remember { mutableStateOf(false) }
    val showSadaqah = remember { mutableStateOf(false) }
    val showAbout = remember { mutableStateOf(false) }
    val showRamadan = remember { mutableStateOf(false) }
    val showFinance = remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    if (showFormation.value) { FormationScreen(onBack = { showFormation.value = false }); return }
    if (showWeekly.value) { WeeklyReviewScreen(onBack = { showWeekly.value = false }); return }
    if (showDiary.value) { DiaryScreen(onBack = { showDiary.value = false }); return }
    if (showStruggle.value) { StruggleScreen(onBack = { showStruggle.value = false }); return }
    if (showCompanion.value) {
        CompanionScreen(onBack = { showCompanion.value = false }, onNavigate = { r ->
            showCompanion.value = false
            when (r) {
                "diary" -> showDiary.value = true
                "tawbah" -> showStruggle.value = true
                "finance" -> showFinance.value = true
                "bedtime" -> ctx.startActivity(Intent(ctx, BedtimeActivity::class.java))
                "dua_journal" -> showDuaJournal.value = true
            }
        })
        return
    }
    if (showLetters.value) { LettersScreen(onBack = { showLetters.value = false }); return }
    if (showDuaJournal.value) { DuaJournalScreen(onBack = { showDuaJournal.value = false }); return }
    if (showFast.value) { FastScreen(onBack = { showFast.value = false }); return }
    if (showSleep.value) { SleepScreen(onBack = { showSleep.value = false }); return }
    if (showBackup.value) { BackupScreen(onBack = { showBackup.value = false }); return }
    if (showDuas.value) { DuaLibraryScreen(onBack = { showDuas.value = false }); return }
    if (showCalendar.value) { CalendarScreen(onBack = { showCalendar.value = false }); return }
    if (showZakat.value) { ZakatScreen(onBack = { showZakat.value = false }); return }
    if (showHifdh.value) { HifdhScreen(onBack = { showHifdh.value = false }); return }
    if (showCharacter.value) { CharacterScreen(onBack = { showCharacter.value = false }); return }
    if (showSunnah.value) { SunnahScreen(onBack = { showSunnah.value = false }); return }
    if (showAdhkar.value) { AdhkarScreen(onBack = { showAdhkar.value = false }); return }
    if (showSadaqah.value) { SadaqahScreen(onBack = { showSadaqah.value = false }); return }
    if (showAbout.value) { AboutScreen(onBack = { showAbout.value = false }); return }
    if (showRamadan.value) { RamadanScreen(onBack = { showRamadan.value = false }); return }
    if (showFinance.value) { FinanceScreen(onBack = { showFinance.value = false }); return }

    val name = Settings.displayName(ctx)
    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // HERO HEADER
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            Image(
                painter = painterResource(id = com.jannahos.R.drawable.dhikr_hero),
                contentDescription = "You",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(bottom = JannahSpacing.m)
            ) {
                Text(
                    if (name.isBlank()) "You." else "$name.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(2.dp))
                Text("Level $level · $prayedWeek of $target nights this week",
                    style = JannahType.caption, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {

            // Formation
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Orange)
                    .pressClickable { showFormation.value = true }
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TAHAJJUD",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when (level) {
                                1 -> "One night a week"
                                2 -> "Two nights a week"
                                3 -> "Three nights a week"
                                else -> "Every night"
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange
                        )
                    }
                    Text("→",
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Primary tools
            SectionLabel("YOUR PRACTICE")
            ListRow("Weekly review") { showWeekly.value = true }
            Divider()
            ListRow("Diary") { showDiary.value = true }
            Divider()
            ListRow("Private commitments") { showStruggle.value = true }
            Divider()
            ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
            Divider()
            ListRow("Dua journal") { showDuaJournal.value = true }
            Divider()
            ListRow("Letters to future self") { showLetters.value = true }
            Divider()
            ListRow("Companion") { showCompanion.value = true }

            Spacer(Modifier.height(JannahSpacing.l))

            // Ramadan (only when relevant)
            val ramadan = com.jannahos.RamadanMode.current()
            if (ramadan.active || ramadan.daysUntil in 1..60) {
                SectionLabel("SEASON")
                ListRow(
                    if (ramadan.active) "Ramadan · day ${ramadan.dayOfRamadan}"
                    else "Ramadan in ${ramadan.daysUntil} days"
                ) { showRamadan.value = true }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Additional — collapsed
            if (showMore) {
                SectionLabel("MORE")
                ListRow("Sunnah prayers") { showSunnah.value = true }
                Divider()
                ListRow("Adhkar") { showAdhkar.value = true }
                Divider()
                ListRow("Dua library") { showDuas.value = true }
                Divider()
                ListRow("Hifdh") { showHifdh.value = true }
                Divider()
                ListRow("Fasting") { showFast.value = true }
                Divider()
                ListRow("Sadaqah log") { showSadaqah.value = true }
                Divider()
                ListRow("Zakat calculator") { showZakat.value = true }
                Divider()
                ListRow("Finance") { showFinance.value = true }
                Divider()
                ListRow("Sleep log") { showSleep.value = true }
                Divider()
                ListRow("Character") { showCharacter.value = true }
                Divider()
                ListRow("Islamic calendar") { showCalendar.value = true }

                Spacer(Modifier.height(JannahSpacing.l))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .pressClickable { showMore = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Show less", style = JannahType.caption, color = JannahColors.Muted)
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Card)
                        .pressClickable { showMore = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("More tools",
                        style = JannahType.button, color = JannahColors.Ink)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Settings
            SectionLabel("SETTINGS")
            ListRow("About") { showAbout.value = true }
            Divider()
            ListRow("Backup & restore") { showBackup.value = true }
            Divider()
            ListRow("Settings") {
                ctx.startActivity(Intent(ctx, SettingsActivity::class.java))
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text,
            style = JannahType.eyebrow,
            color = JannahColors.OrangeDeep,
            modifier = Modifier.padding(top = JannahSpacing.m, bottom = JannahSpacing.s))
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.gutter, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = JannahColors.Ink,
            modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
}
