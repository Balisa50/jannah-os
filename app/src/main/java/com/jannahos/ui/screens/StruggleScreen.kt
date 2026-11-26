package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.StruggleTracker
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun StruggleScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val store = remember { runCatching { StruggleTracker(context) }.getOrNull() }
    var refresh by remember { mutableIntStateOf(0) }
    val habitsResult = remember(refresh) { runCatching { store?.habits().orEmpty() } }
    val habits = habitsResult.getOrDefault(emptyList())

    var showAdd by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var pendingSlipId by remember { mutableStateOf<Long?>(null) }
    var slipReason by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(
            title = "Commitments",
            eyebrow = "PRIVATE",
            subtitle = "A quiet place to keep trying.",
            onBack = onBack
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            Spacer(Modifier.height(JannahSpacing.l))
            Text(
                "Name a habit you want to leave. Progress is private; a difficult day is information, not a verdict.",
                style = JannahType.body,
                color = JannahColors.Body,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier.fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable { showAdd = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Add a commitment", style = JannahType.button, color = JannahColors.OnOrange)
            }

            errorMessage?.let {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(it, style = JannahType.caption, color = JannahColors.Danger)
            }

            Spacer(Modifier.height(JannahSpacing.l))
            if (store == null || habitsResult.isFailure) {
                Text("Private tracking storage couldn't be opened. Your existing commitments have not been changed.", style = JannahType.bodySmall, color = JannahColors.Danger)
            } else if (habits.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Start with one thing.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(6.dp))
                    Text("You can add or remove commitments at any time.", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else {
                Text("YOUR COMMITMENTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                habits.forEach { habit ->
                    val days = runCatching { store.currentDays(habit) }.getOrDefault(0).coerceAtLeast(0)
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.m)
                    ) {
                        Text(habit.label, fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(days.toString(), fontFamily = FontFamily.Serif, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                            Spacer(Modifier.width(8.dp))
                            Text(if (days == 1) "day since your last reset" else "days since your last reset", style = JannahType.caption, color = JannahColors.Muted, modifier = Modifier.padding(bottom = 5.dp))
                        }
                        Text("Best so far: ${maxOf(habit.bestStreak, days)} days", style = JannahType.micro, color = JannahColors.Muted)
                        Spacer(Modifier.height(JannahSpacing.m))
                        Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)) {
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(50))
                                    .background(JannahColors.OrangeSoft)
                                    .clickable { pendingSlipId = habit.id; slipReason = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Log a slip", style = JannahType.button, color = JannahColors.OrangeDeep)
                            }
                            Box(
                                Modifier.height(44.dp).clip(RoundedCornerShape(50))
                                    .background(JannahColors.SurfaceAlt)
                                    .clickable { pendingDeleteId = habit.id }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Remove", style = JannahType.caption, color = JannahColors.Muted)
                            }
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add a commitment", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Use a name that makes sense to you. This stays encrypted on this device.", style = JannahType.bodySmall, color = JannahColors.Muted)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it.take(80); errorMessage = null },
                        label = { Text("Habit or commitment") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val label = newLabel.trim()
                    if (label.isBlank()) {
                        errorMessage = "Give this commitment a name first."
                    } else {
                        runCatching { store?.addHabit(label) ?: error("Private storage is unavailable.") }
                            .onSuccess { newLabel = ""; showAdd = false; refresh++; errorMessage = null }
                            .onFailure { errorMessage = it.message ?: "Could not save this commitment." }
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
            containerColor = JannahColors.Surface
        )
    }

    pendingSlipId?.let { habitId ->
        AlertDialog(
            onDismissRequest = { pendingSlipId = null },
            title = { Text("Record a difficult moment", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This resets the current count, not your ability to begin again. A note is optional.", style = JannahType.bodySmall, color = JannahColors.Muted)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = slipReason,
                        onValueChange = { slipReason = it.take(240) },
                        label = { Text("Optional note") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { store?.recordSlip(habitId, slipReason.trim().takeIf { it.isNotBlank() }) }
                        .onSuccess { pendingSlipId = null; slipReason = ""; refresh++ }
                        .onFailure { errorMessage = it.message ?: "Could not record this entry." }
                }) { Text("Record") }
            },
            dismissButton = { TextButton(onClick = { pendingSlipId = null }) { Text("Cancel") } },
            containerColor = JannahColors.Surface
        )
    }

    pendingDeleteId?.let { habitId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Remove this commitment?") },
            text = { Text("Its history and recorded slips will also be deleted from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { store?.deleteHabit(habitId) }
                        .onSuccess { pendingDeleteId = null; refresh++ }
                        .onFailure { errorMessage = it.message ?: "Could not remove this commitment." }
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Keep") } },
            containerColor = JannahColors.Surface
        )
    }
}
