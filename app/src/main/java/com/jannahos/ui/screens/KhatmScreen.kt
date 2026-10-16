package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
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
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val planDays = KhatmPlan.planDays(ctx)

    if (planDays == 0) {
        ChoosePlan(onBack = onBack) { days ->
            KhatmPlan.setPlan(ctx, days)
            refresh++
        }
        return
    }

    val plan = remember { KhatmPlan.planForDays(planDays) }
    val done = KhatmPlan.completedDays(ctx)
    val nextDay = KhatmPlan.nextDay(ctx)
    val percent = (done.size.toFloat() / planDays) * 100

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
                Text("KHATM · $planDays DAYS",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("${done.size} of $planDays ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("days read.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("${percent.toInt()}% through the Quran",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Progress bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(JannahColors.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(percent / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(JannahColors.Orange)
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Today's portion (the next unread day)
            if (nextDay != null) {
                val portion = plan[nextDay - 1]
                Text("TODAY'S PORTION · DAY ${portion.day}",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(portion.label,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink,
                            lineHeight = 26.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("Read it. Then mark it as done.",
                            style = JannahType.caption, color = JannahColors.Muted)
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.Orange)
                                .clickable {
                                    KhatmPlan.toggleDay(ctx, portion.day)
                                    refresh++
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text("Mark day ${portion.day} as read",
                                style = JannahType.button, color = JannahColors.OnOrange)
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
                        Text("COMPLETE",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("You have finished the entire Quran in $planDays days. Alhamdulillah.",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            color = JannahColors.Ink,
                            lineHeight = 26.sp)
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
                        .clickable {
                            KhatmPlan.toggleDay(ctx, p.day)
                            refresh++
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when {
                                    isDone -> JannahColors.Orange
                                    isNext -> JannahColors.OrangeSoft
                                    else -> JannahColors.Card
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (isDone) "✓" else "${p.day}",
                            style = JannahType.micro,
                            color = when {
                                isDone -> JannahColors.OnOrange
                                isNext -> JannahColors.OrangeDeep
                                else -> JannahColors.Muted
                            }
                        )
                    }
                    Spacer(Modifier.width(JannahSpacing.s))
                    Text(p.label,
                        style = JannahType.bodySmall,
                        color = if (isDone) JannahColors.Muted else JannahColors.Body,
                        modifier = Modifier.weight(1f))
                }
                if (i < plan.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
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
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun ChoosePlan(onBack: () -> Unit, onPick: (Int) -> Unit) {
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
                Text("KHATM",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Finish the ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("Quran.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Read it in 30, 60, or 90 days. Your pace.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            val options = listOf(
                30 to "30 days · one juz' a day",
                60 to "60 days · half a juz' a day",
                90 to "90 days · gentle pace"
            )

            options.forEachIndexed { i, (days, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(days) }
                        .padding(vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("$days days",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(label,
                            style = JannahType.caption,
                            color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
                if (i < options.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Text(
                    "The plan is a suggestion. Missed days do not reset anything. Return when you can. The Quran is a lifetime companion, not a race.",
                    style = JannahType.bodySmall,
                    color = JannahColors.Body,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
