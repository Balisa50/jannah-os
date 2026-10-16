package com.jannahos.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BedtimeActivity
import com.jannahos.PlannerActivity
import com.jannahos.SafeStore
import com.jannahos.Settings
import com.jannahos.SettingsActivity
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    // ---- Modal state ----
    val showFormation = remember { mutableStateOf(false) }
    val showDiary = remember { mutableStateOf(false) }
    val showStruggle = remember { mutableStateOf(false) }
    val showWeekly = remember { mutableStateOf(false) }

    // ---- Labs (moved off the main list) ----
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
    val showFinance = remember { mutableStateOf(false) }
    val showTime = remember { mutableStateOf(false) }
    val showLetters = remember { mutableStateOf(false) }
    val showCompanion = remember { mutableStateOf(false) }
    val showDuaJournal = remember { mutableStateOf(false) }
    val showGraduation = remember { mutableStateOf(false) }
    val showAbout = remember { mutableStateOf(false) }
    val showRamadan = remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    // Route the modals
    if (showFormation.value) { FormationScreen(onBack = { showFormation.value = false }); return }
    if (showWeekly.value) { WeeklyReviewScreen(onBack = { showWeekly.value = false }); return }
    if (showDiary.value) { DiaryScreen(onBack = { showDiary.value = false }); return }
    if (showStruggle.value) { StruggleScreen(onBack = { showStruggle.value = false }); return }
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
    if (showFinance.value) { FinanceScreen(onBack = { showFinance.value = false }); return }
    if (showTime.value) { TimeScreen(onBack = { showTime.value = false }); return }
    if (showLetters.value) { LettersScreen(onBack = { showLetters.value = false }); return }
    if (showCompanion.value) {
        CompanionScreen(
            onBack = { showCompanion.value = false },
            onNavigate = { r ->
                showCompanion.value = false
                when (r) {
                    "dhikr" -> showStruggle.value = true
                    "diary" -> showDiary.value = true
                    "tawbah" -> showStruggle.value = true
                    "finance" -> showFinance.value = true
                    "bedtime" -> ctx.startActivity(Intent(ctx, BedtimeActivity::class.java))
                    "dua_journal" -> showDuaJournal.value = true
                }
            }
        )
        return
    }
    if (showDuaJournal.value) { DuaJournalScreen(onBack = { showDuaJournal.value = false }); return }
    if (showGraduation.value) { GraduationScreen(onBack = { showGraduation.value = false }); return }
    if (showAbout.value) { AboutScreen(onBack = { showAbout.value = false }); return }
    if (showRamadan.value) { RamadanScreen(onBack = { showRamadan.value = false }); return }
    if (showLetters.value) { LettersScreen(onBack = { showLetters.value = false }); return }
    if (showCompanion.value) {
        CompanionScreen(
            onBack = { showCompanion.value = false },
            onNavigate = { r ->
                showCompanion.value = false
                when (r) {
                    "dhikr" -> showStruggle.value = true
                    "diary" -> showDiary.value = true
                    "tawbah" -> showStruggle.value = true
                    "finance" -> showFinance.value = true
                    "bedtime" -> ctx.startActivity(Intent(ctx, BedtimeActivity::class.java))
                    "dua_journal" -> showDuaJournal.value = true
                }
            }
        )
        return
    }
    if (showDuaJournal.value) { DuaJournalScreen(onBack = { showDuaJournal.value = false }); return }
    if (showGraduation.value) { GraduationScreen(onBack = { showGraduation.value = false }); return }
    if (showAbout.value) { AboutScreen(onBack = { showAbout.value = false }); return }
    if (showRamadan.value) { RamadanScreen(onBack = { showRamadan.value = false }); return }
    if (showLetters.value) { LettersScreen(onBack = { showLetters.value = false }); return }
    if (showCompanion.value) {
        CompanionScreen(
            onBack = { showCompanion.value = false },
            onNavigate = { r ->
                showCompanion.value = false
                when (r) {
                    "dhikr" -> showStruggle.value = true
                    "diary" -> showDiary.value = true
                    "tawbah" -> showStruggle.value = true
                    "finance" -> showFinance.value = true
                    "bedtime" -> ctx.startActivity(Intent(ctx, BedtimeActivity::class.java))
                    "dua_journal" -> showDuaJournal.value = true
                }
            }
        )
        return
    }
    if (showDuaJournal.value) { DuaJournalScreen(onBack = { showDuaJournal.value = false }); return }
    if (showGraduation.value) { GraduationScreen(onBack = { showGraduation.value = false }); return }
    if (showAbout.value) { AboutScreen(onBack = { showAbout.value = false }); return }
    if (showRamadan.value) { RamadanScreen(onBack = { showRamadan.value = false }); return }

    val name = Settings.displayName(ctx)
    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val stats = SafeStore.stats30(ctx)
    val nights = stats.verified + stats.claimed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .navigationBarsPadding()
    ) {
        // ==== HEADER ====
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("YOU",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    if (name.isBlank()) "Your formation."
                    else "$name, your formation.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.OnOrange,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$nights nights in 30 · Level $level · $prayedWeek of $target this week",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // ==== TIER 1, the five pillars of the product ====

            // Formation, the hero of this screen
            BigTile(
                eyebrow = "TAHAJJUD FORMATION",
                title = when (level) {
                    1 -> "One night a week"
                    2 -> "Two nights a week"
                    3 -> "Three nights a week"
                    else -> "Every night"
                },
                subtitle = "$nights of last 30 nights",
                onClick = { showFormation.value = true },
                accent = true
            )

            Spacer(Modifier.height(JannahSpacing.s))

            // Weekly review
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                SmallTile(
                    modifier = Modifier.weight(1f),
                    eyebrow = "REVIEW",
                    title = "Weekly",
                    onClick = { showWeekly.value = true }
                )
                SmallTile(
                    modifier = Modifier.weight(1f),
                    eyebrow = "PRIVATE",
                    title = "Diary",
                    onClick = { showDiary.value = true }
                )
            }

            Spacer(Modifier.height(JannahSpacing.s))

            // Your Time, the deeds engine
            SmallTile(
                modifier = Modifier.fillMaxWidth(),
                eyebrow = "EVERY MINUTE COUNTS",
                title = "What can I do right now?",
                onClick = { showTime.value = true }
            )

            Spacer(Modifier.height(JannahSpacing.s))

            // Commitments + Tonight's plan
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                SmallTile(
                    modifier = Modifier.weight(1f),
                    eyebrow = "LEAVE IT",
                    title = "Commitments",
                    onClick = { showStruggle.value = true }
                )
                SmallTile(
                    modifier = Modifier.weight(1f),
                    eyebrow = "SLEEP",
                    title = "Tonight's plan",
                    onClick = { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
                )
            }

            Spacer(Modifier.height(JannahSpacing.s))

            // Companion, the doorway
            SmallTile(
                modifier = Modifier.fillMaxWidth(),
                eyebrow = "TELL ME",
                title = "What's on your mind?",
                onClick = { showCompanion.value = true }
            )

            Spacer(Modifier.height(JannahSpacing.s))

            // Letters to future self
            SmallTile(
                modifier = Modifier.fillMaxWidth(),
                eyebrow = "SEALED",
                title = "Letters to future self",
                onClick = { showLetters.value = true }
            )

            Spacer(Modifier.height(JannahSpacing.s))

            // Dua journal
            SmallTile(
                modifier = Modifier.fillMaxWidth(),
                eyebrow = "RECORD",
                title = "Dua journal, answered duas",
                onClick = { showDuaJournal.value = true }
            )



            Spacer(Modifier.height(JannahSpacing.xl))

            // ==== SETTINGS, always visible, not buried ====
            Text("SETTINGS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
            ) {
                Column {
                    ListRow("About Jannah") { showAbout.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Location & calculation") {
                        ctx.startActivity(Intent(ctx, SettingsActivity::class.java))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Backup & restore") { showBackup.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow(
                        if (showMore) "Hide additional tools" else "Show additional tools"
                    ) { showMore = !showMore }
                }
            }

            // ==== TIER 2, additional tools, hidden by default ====
            if (showMore) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("ADDITIONAL TOOLS",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("Core to some, secondary to others. Here when you need them.",
                    style = JannahType.caption, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                ) {
                    Column {
                        ListRow("Planner") { ctx.startActivity(Intent(ctx, PlannerActivity::class.java)) }
                        Divider()
                        ListRow("Fasting tracker") { showFast.value = true }
                        Divider()
                        ListRow("Sadaqah log") { showSadaqah.value = true }
                        Divider()
                        ListRow("Zakat calculator") { showZakat.value = true }
                        Divider()
                        ListRow("Islamic calendar") { showCalendar.value = true }
                        Divider()
                        ListRow("Dua library") { showDuas.value = true }
                        Divider()
                        ListRow("Adhkar (morning & evening)") { showAdhkar.value = true }
                        Divider()
                        ListRow("Sunnah prayers") { showSunnah.value = true }
                        Divider()
                        ListRow("Hifdh, memorize Quran") { showHifdh.value = true }
                        Divider()
                        ListRow("Character & daily hadith") { showCharacter.value = true }
                        Divider()
                        ListRow("Sleep log") { showSleep.value = true }
                        Divider()
                        ListRow("Finance, the middle path") { showFinance.value = true }
                        Divider()
                        ListRow("Graduation") { showGraduation.value = true }
                        Divider()
                        ListRow("Ramadan") { showRamadan.value = true }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun BigTile(
    eyebrow: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accent: Boolean = false
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(if (accent) JannahColors.Orange else JannahColors.Card)
            .clickable(onClick = onClick)
            .padding(JannahSpacing.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow,
                    style = JannahType.eyebrow,
                    color = if (accent) JannahColors.OnOrange.copy(alpha = 0.75f) else JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (accent) JannahColors.OnOrange else JannahColors.Ink,
                    lineHeight = 30.sp)
                Spacer(Modifier.height(4.dp))
                Text(subtitle,
                    style = JannahType.caption,
                    color = if (accent) JannahColors.OnOrange.copy(alpha = 0.85f) else JannahColors.Muted)
            }
            Text("→",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (accent) JannahColors.OnOrange else JannahColors.Orange)
        }
    }
}

@Composable
private fun SmallTile(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .clickable(onClick = onClick)
            .padding(JannahSpacing.l)
    ) {
        Column {
            Text(eyebrow,
                style = JannahType.eyebrow,
                color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(title,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                lineHeight = 24.sp)
        }
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.l, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
}
