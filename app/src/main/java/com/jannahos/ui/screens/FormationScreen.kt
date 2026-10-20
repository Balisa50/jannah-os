package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FrictionMap
import com.jannahos.TahajjudPlan
import com.jannahos.WakeLog
import com.jannahos.ui.TopBar
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
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(JannahColors.Orange, JannahColors.OrangeDeep)
                    )
                )
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xxl, bottom = JannahSpacing.xl)
        ) {
            Column {
                Text("FORMATION", style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Thirty ", style = JannahType.title.copy(fontSize = 28.sp),
                        color = JannahColors.OnOrange)
                    Text("nights.", style = JannahType.titleItalic.copy(fontSize = 28.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            
            // Big number: verified
            Text("VERIFIED RECITATIONS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${stats.verified}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = (-2).sp,
                    lineHeight = 68.sp
                )
                Spacer(Modifier.width(8.dp))
                Text("nights", style = JannahType.title.copy(fontSize = 18.sp),
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 12.dp))
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            Spacer(Modifier.height(JannahSpacing.m))

            // Three-column breakdown
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCol("${stats.verified}", "Verified", JannahColors.Orange)
                StatCol("${stats.claimed}", "Claimed", JannahColors.Ink)
                StatCol("${stats.missed}", "Missed", JannahColors.Muted)
            }
            
            Spacer(Modifier.height(JannahSpacing.m))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            Spacer(Modifier.height(JannahSpacing.m))

            Text(
                if (stats.verified + stats.claimed > 0)
                    "${(stats.verificationRate * 100).toInt()}% of your prayers were recited aloud and recognised."
                else
                    "No prayers logged yet. Your first night starts the record.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            // Current formation
            Text("CURRENT LEVEL", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
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
            Spacer(Modifier.height(4.dp))
            Text("$prayedWeek of $target this week", style = JannahType.caption, color = JannahColors.Muted)
            
            Spacer(Modifier.height(JannahSpacing.m))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            Spacer(Modifier.height(JannahSpacing.l))

            // Pattern insight
            if (insight != null) {
                Text("WHAT BLOCKS YOU", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    "${insight.label} · ${insight.count}x",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    insight.suggestion,
                    style = JannahType.bodySmall,
                    color = JannahColors.Orange,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(JannahSpacing.m))
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Recent nights list
            if (history.isNotEmpty()) {
                Text("RECENT NIGHTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
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
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(6.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(dotColor)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(date, style = JannahType.body, color = JannahColors.Ink)
                        }
                        Text(label, style = JannahType.caption,
                            color = if (e.outcome == "prayed") JannahColors.Orange else JannahColors.Muted)
                    }
                    if (i < history.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
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

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun StatCol(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = JannahType.caption, color = JannahColors.Muted)
    }
}
