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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BedtimeActivity
import com.jannahos.PlannerActivity
import com.jannahos.SettingsActivity
import com.jannahos.TahajjudPlan
import com.jannahos.UrgeActivity
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val showFormation = remember { mutableStateOf(false) }
    val showTawbah = remember { mutableStateOf(false) }
    val showWeekly = remember { mutableStateOf(false) }
    val showFast = remember { mutableStateOf(false) }
    val showSleep = remember { mutableStateOf(false) }
    val showBackup = remember { mutableStateOf(false) }
    val showDuas = remember { mutableStateOf(false) }
    val showCalendar = remember { mutableStateOf(false) }
    val showZakat = remember { mutableStateOf(false) }
    val showHifdh = remember { mutableStateOf(false) }
    val showCharacter = remember { mutableStateOf(false) }
    val showSunnah = remember { mutableStateOf(false) }
    val showDiary = remember { mutableStateOf(false) }

    if (showFormation.value) {
        FormationScreen(onBack = { showFormation.value = false })
        return
    }
    if (showTawbah.value) {
        TawbahScreen(onBack = { showTawbah.value = false })
        return
    }
    if (showWeekly.value) {
        WeeklyReviewScreen(onBack = { showWeekly.value = false })
        return
    }
    if (showFast.value) {
        FastScreen(onBack = { showFast.value = false })
        return
    }
    if (showSleep.value) {
        SleepScreen(onBack = { showSleep.value = false })
        return
    }
    if (showBackup.value) {
        BackupScreen(onBack = { showBackup.value = false })
        return
    }
    if (showDuas.value) {
        DuaLibraryScreen(onBack = { showDuas.value = false })
        return
    }
    if (showCalendar.value) {
        CalendarScreen(onBack = { showCalendar.value = false })
        return
    }
    if (showZakat.value) {
        ZakatScreen(onBack = { showZakat.value = false })
        return
    }
    if (showHifdh.value) {
        HifdhScreen(onBack = { showHifdh.value = false })
        return
    }
    if (showCharacter.value) {
        CharacterScreen(onBack = { showCharacter.value = false })
        return
    }
    if (showSunnah.value) {
        SunnahScreen(onBack = { showSunnah.value = false })
        return
    }
    if (showDiary.value) {
        DiaryScreen(onBack = { showDiary.value = false })
        return
    }
    if (showSunnah.value) {
        SunnahScreen(onBack = { showSunnah.value = false })
        return
    }
    if (showDiary.value) {
        DiaryScreen(onBack = { showDiary.value = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
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
                Row {
                    Text("Your ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("formation.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Formation card
            Text("FORMATION", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            when (level) {
                                1 -> "One night "
                                2 -> "Two nights "
                                3 -> "Three nights "
                                else -> "Every night "
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink
                        )
                        Text("a week.",
                            fontFamily = FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Orange)
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    Text("$prayedWeek of $target this week",
                        style = JannahType.caption, color = JannahColors.Muted)

                    Spacer(Modifier.height(JannahSpacing.m))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                    ) {
                        listOf(1, 2, 3, 7).forEach { lvl ->
                            val selected = level == lvl
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(if (selected) JannahColors.Orange else JannahColors.SurfaceAlt)
                                    .clickable {
                                        TahajjudPlan.setLevel(ctx, lvl)
                                        refresh = refresh + 1
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$lvl",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) JannahColors.OnOrange else JannahColors.Ink
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    Text("Nights per week", style = JannahType.caption, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Tools
            Text("TOOLS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
            ) {
                Column {
                    ListRow("Formation record") { showFormation.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Weekly review") { showWeekly.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Fasting") { showFast.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Sleep log") { showSleep.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Dua library") { showDuas.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Islamic calendar") { showCalendar.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Zakat calculator") { showZakat.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Hifdh — memorize Quran") { showHifdh.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Character & daily hadith") { showCharacter.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Sunnah prayers") { showSunnah.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Diary") { showDiary.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Sunnah prayers") { showSunnah.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Diary") { showDiary.value = true }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Planner") { ctx.startActivity(Intent(ctx, PlannerActivity::class.java)) }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Urge support") { ctx.startActivity(Intent(ctx, UrgeActivity::class.java)) }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Tawbah — return to Allah") { showTawbah.value = true }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Settings
            Text("SETTINGS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
            ) {
                Column {
                    ListRow("Location & calculation") {
                        ctx.startActivity(Intent(ctx, SettingsActivity::class.java))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    ListRow("Backup & restore") { showBackup.value = true }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.l, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}
