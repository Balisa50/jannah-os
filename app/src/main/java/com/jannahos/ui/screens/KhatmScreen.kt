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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.KhatmPlan
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun KhatmScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val ignore = refresh

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
    val percent = if (planDays > 0) (done.size.toFloat() / planDays) * 100 else 0f

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

            // Progress bar
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

            // Today's portion
            if (nextDay != null) {
                val portion = plan[nextDay - 1]
                Text("TODAY · DAY ${portion.day}",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Orange)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(portion.label,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange,
                            lineHeight = 28.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("${portion.ayahCount} ayahs",
                            style = JannahType.caption,
                            color = JannahColors.OnOrange.copy(alpha = 0.85f))
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.OnOrange)
                                .clickable {
                                    KhatmPlan.toggleDay(ctx, portion.day)
                                    refresh++
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text("Mark day ${portion.day} done",
                                style = JannahType.button, color = JannahColors.OrangeDeep)
                        }
                    }
                }
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
                        Text("The Quran has been finished in $planDays days. Alhamdulillah.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Full plan list
            Text("THE FULL PLAN",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            plan.forEachIndexed { i, p ->
                val isDone = done.contains(p.day)
                val isNext = nextDay == p.day
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isNext -> JannahColors.OrangeSoft
                                else -> JannahColors.Card
                            }
                        )
                        .clickable {
                            KhatmPlan.toggleDay(ctx, p.day)
                            refresh++
                        }
                        .padding(horizontal = JannahSpacing.m, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isDone) JannahColors.Orange else JannahColors.Cream
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${p.day}",
                            style = JannahType.micro,
                            color = if (isDone) JannahColors.OnOrange else JannahColors.Muted,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(p.label,
                        style = JannahType.bodySmall,
                        color = if (isDone) JannahColors.Muted else JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    if (isDone) {
                        Text("✓", style = JannahType.body, color = JannahColors.Orange)
                    }
                }
                if (i < plan.size - 1) Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Card)
                        .clickable {
                            KhatmPlan.clear(ctx)
                            refresh++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Start over", style = JannahType.caption, color = JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
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
                onValueChange = { s ->
                    // numbers only, max 3 digits
                    daysText = s.filter { it.isDigit() }.take(3)
                },
                label = { Text("Days", fontSize = 12.sp) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(JannahSpacing.s))

            // Quick picks
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

            // Preview
            if (days != null && days in 1..365) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("PREVIEW · FIRST 3 DAYS",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                val preview = remember(days) { KhatmPlan.planForDays(days) }
                preview.take(3).forEach { p ->
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
                            Spacer(Modifier.height(2.dp))
                            Text(p.label,
                                fontFamily = FontFamily.Serif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = JannahColors.Ink)
                            Text("${p.ayahCount} ayahs",
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (days > 3) {
                    Text("… and ${days - 3} more",
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
