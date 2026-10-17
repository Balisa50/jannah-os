package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.KhatmPlan
import com.jannahos.QuranCache
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun KhatmScreen(onBack: () -> Unit) {
    SwipeBackScreen(onBack = onBack) {
        KhatmContent(onBack = onBack)
    }
}

@Composable
private fun KhatmContent(onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val ignore = refresh

    var openPage by remember { mutableStateOf<Int?>(null) }
    if (openPage != null) {
        MushafPageScreen(startPage = openPage!!, onBack = { openPage = null })
        return
    }

    val planDays = KhatmPlan.planDays(ctx)
    if (planDays == 0) {
        ChoosePlan(onBack = onBack) { days ->
            KhatmPlan.setPlan(ctx, days)
            refresh++
        }
        return
    }

    val plan = remember(planDays, refresh) { KhatmPlan.planForDays(planDays) }
    val done = KhatmPlan.completedDays(ctx)
    val nextDay = KhatmPlan.nextDay(ctx)
    val percent = (done.size.toFloat() / planDays) * 100

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        TopBar("Reading plan", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.s))

            Row(verticalAlignment = Alignment.Bottom) {
                Text("${done.size}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = (-1).sp)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("of $planDays days",
                    style = JannahType.body,
                    color = JannahColors.Muted,
                    modifier = Modifier.padding(bottom = 8.dp))
            }

            Spacer(Modifier.height(JannahSpacing.s))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(JannahColors.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(JannahColors.Orange)
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            if (nextDay != null) {
                val portion = plan[nextDay - 1]
                PortionCard(
                    portion = portion,
                    isToday = true,
                    isDone = false,
                    onMarkDone = { KhatmPlan.toggleDay(ctx, portion.day); refresh++ },
                    onOpenReader = { openPage = KhatmPlan.pageFor(portion.startSurah, portion.startAyah) }
                )
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("Complete.",
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("The Quran has been finished in $planDays days.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            Text("THE FULL PLAN",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            plan.forEachIndexed { i, p ->
                val isDone = done.contains(p.day)
                val isNext = nextDay == p.day
                PortionCard(
                    portion = p,
                    isToday = isNext,
                    isDone = isDone,
                    onMarkDone = { KhatmPlan.toggleDay(ctx, p.day); refresh++ },
                    onOpenReader = { openPage = KhatmPlan.pageFor(p.startSurah, p.startAyah) }
                )
                if (i < plan.size - 1) Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .clickable { KhatmPlan.clear(ctx); refresh++ },
                contentAlignment = Alignment.Center
            ) {
                Text("Start over", style = JannahType.caption, color = JannahColors.Muted)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun PortionCard(
    portion: KhatmPlan.Portion,
    isToday: Boolean,
    isDone: Boolean,
    onMarkDone: () -> Unit,
    onOpenReader: () -> Unit
) {
    val surahNameStart = QuranCache.SURAHS
        .firstOrNull { it.first == portion.startSurah }?.second ?: "Surah ${portion.startSurah}"
    val surahNameEnd = QuranCache.SURAHS
        .firstOrNull { it.first == portion.endSurah }?.second ?: "Surah ${portion.endSurah}"

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(if (isToday) JannahColors.Orange else JannahColors.Card)
            .padding(JannahSpacing.l)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isToday) "TODAY · DAY ${portion.day}" else "DAY ${portion.day}",
                    style = JannahType.eyebrow,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.85f)
                            else JannahColors.OrangeDeep
                )
                Spacer(Modifier.weight(1f))
                Text("${portion.ayahCount} ayahs",
                    style = JannahType.caption,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.75f)
                            else JannahColors.Muted)
            }
            Spacer(Modifier.height(JannahSpacing.m))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Start",
                    style = JannahType.micro,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.75f)
                            else JannahColors.Faint,
                    modifier = Modifier.width(54.dp))
                Text("$surahNameStart",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) JannahColors.OnOrange else JannahColors.Ink)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("ayah ${portion.startAyah}",
                    style = JannahType.bodySmall,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.85f)
                            else JannahColors.Muted)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stop",
                    style = JannahType.micro,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.75f)
                            else JannahColors.Faint,
                    modifier = Modifier.width(54.dp))
                Text("$surahNameEnd",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) JannahColors.OnOrange else JannahColors.Ink)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("ayah ${portion.endAyah}",
                    style = JannahType.bodySmall,
                    color = if (isToday) JannahColors.OnOrange.copy(alpha = 0.85f)
                            else JannahColors.Muted)
            }
            Spacer(Modifier.height(JannahSpacing.m))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(
                            if (isToday) JannahColors.OnOrange.copy(alpha = 0.18f)
                            else JannahColors.Cream
                        )
                        .clickable(onClick = onOpenReader),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Open in mushaf",
                        style = JannahType.caption,
                        color = if (isToday) JannahColors.OnOrange else JannahColors.Ink,
                        fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(
                            when {
                                isDone -> JannahColors.OrangeSoft
                                isToday -> JannahColors.OnOrange
                                else -> JannahColors.Orange
                            }
                        )
                        .clickable(onClick = onMarkDone),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isDone) "✓ Done" else "Mark done",
                        style = JannahType.caption,
                        color = when {
                            isDone -> JannahColors.OrangeDeep
                            isToday -> JannahColors.OrangeDeep
                            else -> JannahColors.OnOrange
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoosePlan(onBack: () -> Unit, onPick: (Int) -> Unit) {
    BackHandler(onBack = onBack)
    var daysText by remember { mutableStateOf("30") }
    val days = daysText.toIntOrNull()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        TopBar("Reading plan", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("In how many days?",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                lineHeight = 34.sp)

            Spacer(Modifier.height(JannahSpacing.s))

            Text("The Quran divides evenly across your days.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted)

            Spacer(Modifier.height(JannahSpacing.l))

            OutlinedTextField(
                value = daysText,
                onValueChange = { s -> daysText = s.filter { it.isDigit() }.take(3) },
                label = { Text("Days", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(JannahSpacing.s))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(30, 60, 90).forEach { d ->
                    val selected = days == d
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (selected) JannahColors.Orange else JannahColors.Card)
                            .clickable { daysText = d.toString() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$d",
                            style = JannahType.button,
                            color = if (selected) JannahColors.OnOrange else JannahColors.Ink)
                    }
                }
            }

            if (days != null && days in 1..365) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("PREVIEW · FIRST 3 DAYS",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                val preview = remember(days) { KhatmPlan.planForDays(days) }
                preview.take(3).forEach { p ->
                    val sStart = QuranCache.SURAHS
                        .firstOrNull { it.first == p.startSurah }?.second ?: ""
                    val sEnd = QuranCache.SURAHS
                        .firstOrNull { it.first == p.endSurah }?.second ?: ""
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.m)
                    ) {
                        Column {
                            Text("Day ${p.day}",
                                style = JannahType.micro, color = JannahColors.OrangeDeep)
                            Spacer(Modifier.height(4.dp))
                            Text("$sStart ${p.startAyah}  →  $sEnd ${p.endAyah}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text("${p.ayahCount} ayahs",
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (days > 3) {
                    Text("… and ${days - 3} more days",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(
                        if (days == null || days !in 1..365) JannahColors.Border
                        else JannahColors.Ink
                    )
                    .clickable {
                        val d = days ?: return@clickable
                        if (d in 1..365) onPick(d)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Start the plan",
                    style = JannahType.button,
                    color = if (days == null || days !in 1..365)
                        JannahColors.Faint else JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
