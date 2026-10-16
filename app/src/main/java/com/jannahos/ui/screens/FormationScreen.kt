package com.jannahos.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CareMode
import com.jannahos.FrictionMap
import com.jannahos.InterventionLog
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FormationScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val log = remember { WakeLog(ctx) }
    val stats = remember { log.formationStats(30) }
    val history = remember { log.lastNights(14) }
    val insight = remember { FrictionMap.topInsight(ctx) }
    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val dayFmt = SimpleDateFormat("EEE d MMM", Locale.getDefault())

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
                Text("FORMATION", style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Thirty ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("nights.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Your honest record — nothing is uploaded.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Big number: verified
            Text("VERIFIED RECITATIONS", style = JannahType.eyebrow,
                color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${stats.verified}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = (-2).sp,
                    lineHeight = 76.sp
                )
                Spacer(Modifier.width(8.dp))
                Text("nights", style = JannahType.title.copy(fontSize = 18.sp),
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 14.dp))
            }
            Text(
                "You recited Al-Fatihah aloud on these nights and the app matched it.",
                style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp
            )

            Spacer(Modifier.height(JannahSpacing.l))

            // Three-column breakdown card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("LAST 30 NIGHTS", style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCol("${stats.verified}", "Verified", JannahColors.Orange)
                        StatCol("${stats.claimed}", "Claimed", JannahColors.Ink)
                        StatCol("${stats.missed}", "Missed", JannahColors.Muted)
                    }
                    Spacer(Modifier.height(JannahSpacing.m))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JannahColors.Divider)
                    )
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text(
                        if (stats.verified + stats.claimed > 0)
                            "${(stats.verificationRate * 100).toInt()}% of your prayers were recited aloud and recognised."
                        else
                            "No prayers logged yet. Your first night starts the record.",
                        style = JannahType.caption,
                        color = JannahColors.Muted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Care Mode note
            val care = remember { CareMode.evaluate(ctx) }
            if (care.active) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("CARING SEASON",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "Something has been heavy. Numbers still log, but nothing is pushed. This pass, rest. " + CareMode.CAVEAT,
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Current formation
            Text("CURRENT LEVEL", style = JannahType.eyebrow,
                color = JannahColors.OrangeDeep)
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
                                1 -> "One "
                                2 -> "Two "
                                3 -> "Three "
                                else -> "Every "
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink
                        )
                        Text(
                            if (level == 7) "night." else "nights a week.",
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Orange
                        )
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    Text("$prayedWeek of $target this week",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
            }

            // Pattern insight
            if (insight != null) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("WHAT BLOCKS YOU", style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(
                            "${insight.label} · ${insight.count}x",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink
                        )
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            insight.suggestion,
                            style = JannahType.bodySmall,
                            color = JannahColors.Orange,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // What you actually act on
            val statsList = remember { InterventionLog(ctx).stats(30).filter { it.shown >= 2 } }
            if (statsList.isNotEmpty()) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("WHAT MOVES YOU", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(
                            "Over the last 30 days, when the app suggested these, you acted:",
                            style = JannahType.caption, color = JannahColors.Muted,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(JannahSpacing.s))
                        statsList.take(4).forEach { st ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(InterventionLog.label(st.kind),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JannahColors.Ink)
                                Text("${(st.rate * 100).toInt()}%",
                                    style = JannahType.bodySmall,
                                    color = JannahColors.Orange,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "No points. No streaks. Just what works for you.",
                            style = JannahType.micro, color = JannahColors.Faint,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Recent nights list
            if (history.isNotEmpty()) {
                Spacer(Modifier.height(JannahSpacing.l))
                Text("RECENT NIGHTS", style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                history.forEachIndexed { i, e ->
                    val date = dayFmt.format(Date(e.ts))
                    val label = when (e.outcome) {
                        "prayed" -> if (e.voicePath != null) "Recited" else "Claimed"
                        "missed" -> "Missed"
                        else -> e.outcome
                    }
                    val dotColor = when {
                        e.outcome == "prayed" && e.voicePath != null -> JannahColors.Orange
                        e.outcome == "prayed" -> JannahColors.Ink
                        else -> JannahColors.Border
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(4.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(dotColor)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(date,
                                style = JannahType.body,
                                color = JannahColors.Ink)
                        }
                        Text(label,
                            style = JannahType.caption,
                            color = if (e.outcome == "prayed") JannahColors.Orange else JannahColors.Muted)
                    }
                    if (i < history.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            } else {
                Spacer(Modifier.height(JannahSpacing.l))
                Text(
                    "Your nights will appear here after your first tahajjud alarm.",
                    style = JannahType.bodySmall,
                    color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Back pill
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

@Composable
private fun StatCol(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = JannahType.caption, color = JannahColors.Muted)
    }
}
