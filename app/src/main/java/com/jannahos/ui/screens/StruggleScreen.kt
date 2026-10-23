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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Mercy
import com.jannahos.StruggleTracker
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun StruggleScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    val tracker = remember { StruggleTracker(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val habits = tracker.habits()

    var adding by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }

    // Slip flow for one habit
    var slippingHabit by remember { mutableStateOf<StruggleTracker.Habit?>(null) }
    var slipReason by remember { mutableStateOf<String?>(null) }
    var slipMessage by remember { mutableStateOf<String?>(null) }

    val mercy = remember { Mercy.random() }

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
                Text("PRIVATE",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Leave it, ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("gently.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Stays on your phone.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Slip mercy message, after user reports a slip
            if (slipMessage != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("RECORDED",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(mercy.text,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = JannahColors.Ink,
                            lineHeight = 24.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(mercy.source,
                            style = JannahType.micro, color = JannahColors.Faint)
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.Ink)
                                .clickable { slipMessage = null; slippingHabit = null }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text("Close", style = JannahType.button,
                                color = JannahColors.OnOrange)
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Slip reason selection
            if (slippingHabit != null && slipMessage == null) {
                val h = slippingHabit!!
                Text("WHAT HAPPENED?",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("So the pattern is honest.",
                    style = JannahType.caption, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.m))

                val reasons = listOf(
                    "bored" to "I was bored",
                    "lonely" to "I felt lonely",
                    "stressed" to "I was stressed",
                    "late night" to "It was late",
                    "trigger" to "A specific trigger",
                    "no reason" to "No clear reason"
                )
                reasons.forEach { (code, label) ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Card)
                            .clickable {
                                tracker.recordSlip(h.id, code)
                                slipMessage = "ok"
                                refresh++
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(label,
                            style = JannahType.body, color = JannahColors.Ink)
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Card)
                        .clickable { slippingHabit = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", style = JannahType.caption, color = JannahColors.Muted)
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Add habit
            if (adding) {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What are you leaving? (private)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. \"scrolling at night\"", fontSize = 13.sp) }
                )
                Spacer(Modifier.height(JannahSpacing.s))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Card)
                            .clickable { adding = false; newLabel = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = JannahType.button, color = JannahColors.Muted)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (newLabel.isBlank()) JannahColors.Border else JannahColors.Orange)
                            .clickable {
                                if (newLabel.isNotBlank()) {
                                    tracker.addHabit(newLabel)
                                    newLabel = ""
                                    adding = false
                                    refresh++
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add", style = JannahType.button,
                            color = if (newLabel.isBlank()) JannahColors.Faint else JannahColors.OnOrange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable { adding = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+  Add something to leave",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Habits list
            if (habits.isNotEmpty()) {
                Text("YOUR COMMITMENTS",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
            }

            habits.forEachIndexed { i, h ->
                val days = tracker.currentDays(h)
                val slips30 = tracker.slipsFor(h.id, 30).size
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(h.label,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JannahColors.Ink)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    when {
                                        days == 0 -> "Starting today"
                                        days == 1 -> "1 day free"
                                        else -> "$days days free"
                                    },
                                    style = JannahType.bodySmall,
                                    color = JannahColors.Orange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .clickable {
                                        tracker.deleteHabit(h.id)
                                        refresh++
                                    }
                                    .padding(6.dp)
                            ) {
                                Text("×", style = JannahType.body, color = JannahColors.Muted)
                            }
                        }
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            if (slips30 == 0) "No slips in the last 30 days."
                            else "$slips30 slip${if (slips30 == 1) "" else "s"} in the last 30 days.",
                            style = JannahType.caption,
                            color = JannahColors.Muted
                        )
                        if (h.bestStreak > 0) {
                            Spacer(Modifier.height(2.dp))
                            Text("Best: ${h.bestStreak} days",
                                style = JannahType.caption,
                                color = JannahColors.Faint)
                        }
                        Spacer(Modifier.height(JannahSpacing.m))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(JannahColors.OrangeSoft)
                                    .clickable { slippingHabit = h; slipMessage = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("I slipped",
                                    style = JannahType.button,
                                    color = JannahColors.OrangeDeep)
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(JannahColors.Orange)
                                    .clickable { tracker.updateStreak(h.id, days); refresh++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Still free",
                                    style = JannahType.button,
                                    color = JannahColors.OnOrange)
                            }
                        }
                    }
                }
                if (i < habits.size - 1) Spacer(Modifier.height(JannahSpacing.m))
            }

            if (habits.isEmpty() && !adding) {
                Spacer(Modifier.height(JannahSpacing.l))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("A quiet place",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "This space is for habits you want to leave. Name them however you like. Track them only for yourself. No public list, no leaderboard, no exposure.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            
}
