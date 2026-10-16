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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FrictionMap
import com.jannahos.SafeStore
import com.jannahos.TahajjudPlan
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyReviewScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val stats = remember { SafeStore.stats(ctx, 7) }
    val history = remember { SafeStore.lastNights(ctx, 7) }
    val insight = remember { SafeStore.frictionInsight(ctx) }
    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)
    val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

    val adjustment = when {
        insight == null && stats.missed == 0 ->
            "You are holding steady. Keep the same plan for next week."
        insight?.reasonCode == "slept_late" ->
            "This week, plan to stop screens by 22:00. The wake-up gets easier when sleep starts earlier."
        insight?.reasonCode == "alarm_missed" ->
            "This week, try a louder tone for tahajjud, and set a 5-minute backup alarm."
        insight?.reasonCode == "phone_far" ->
            "This week, keep the phone at the head of your bed, screen down."
        insight?.reasonCode == "went_back" ->
            "This week, when you wake, sit up before deciding. Standing up breaks the pull."
        insight?.reasonCode == "too_tired" ->
            "This week, if you slept under six hours, take a short afternoon nap."
        else ->
            "Note what is getting in the way this week. The app will adapt."
    }

    val closing = when {
        stats.missed == 0 -> "A week without a missed night. Alhamdulillah."
        stats.missed <= 2 -> "A strong week. Consistency is built on exactly this kind of week."
        stats.missed <= 4 -> "Half the week held. That is real. Return tomorrow, not after perfection."
        else -> "This was a hard week. Allah sees every attempt. Begin again, gently."
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
                Text("WEEKLY REVIEW",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Your ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("week.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("The week in numbers.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("SHOWED UP", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${stats.verified + stats.claimed}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = (-2).sp,
                    lineHeight = 76.sp)
                Spacer(Modifier.width(8.dp))
                Text("of 7 nights",
                    style = JannahType.title.copy(fontSize = 18.sp),
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(bottom = 14.dp))
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Stat("${stats.verified}", "Recited", JannahColors.Orange)
                    Stat("${stats.claimed}", "Claimed", JannahColors.Ink)
                    Stat("${stats.missed}", "Missed", JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("THE NIGHTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            if (history.isEmpty()) {
                Text("No nights recorded yet.",
                    style = JannahType.bodySmall, color = JannahColors.Muted)
            } else {
                history.forEachIndexed { i, e ->
                    val date = dayFmt.format(Date(e.ts))
                    val label = when {
                        e.outcome == "prayed" && e.voicePath != null -> "Recited Al-Fatihah"
                        e.outcome == "prayed" -> "Claimed"
                        e.outcome == "missed" -> "Missed"
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
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
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
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("PATTERN", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    if (insight != null) {
                        Text("Most often: ${insight.label}",
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                    }
                    Text("Suggestion for next week",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(adjustment,
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 22.sp)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("FORMATION", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Level ", style = JannahType.body, color = JannahColors.Muted)
                    Text("$level",
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink)
                    Spacer(Modifier.width(6.dp))
                    Text("· $prayedWeek of $target this week",
                        style = JannahType.caption,
                        color = JannahColors.Muted,
                        modifier = Modifier.padding(bottom = 4.dp))
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
                Text(closing,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = JannahColors.Ink,
                    lineHeight = 26.sp)
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

@Composable
private fun Stat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value,
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = JannahType.caption, color = JannahColors.Muted)
    }
}
