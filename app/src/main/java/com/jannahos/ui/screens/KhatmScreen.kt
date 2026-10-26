package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jannahos.KhatmStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun KhatmScreen(onBack: () -> Unit, onOpenRange: (Int, Int) -> Unit) {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val target = KhatmStore.getTarget(ctx)
    val completed = KhatmStore.getCompleted(ctx)
    var daysInput by remember { mutableStateOf(target.toString()) }
    var pendingReset by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Reading Plan", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(16.dp))
            Text("CHOOSE YOUR PACE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = daysInput,
                    onValueChange = { daysInput = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Days", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    Modifier.height(56.dp).width(100.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable {
                        val days = daysInput.toIntOrNull() ?: 30
                        if (days > 0 && days <= 604) {
                            if (days == target) refresh++
                            else if (completed > 0) pendingReset = days
                            else {
                                KhatmStore.setTarget(ctx, days)
                                KhatmStore.reset(ctx)
                                refresh++
                            }
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Progress: $completed of $target days", style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(JannahColors.Border)) {
                Box(Modifier.fillMaxWidth(if (target == 0) 0f else completed.toFloat() / target).height(4.dp).clip(RoundedCornerShape(2.dp)).background(JannahColors.Orange))
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = JannahSpacing.gutter),
            contentPadding = PaddingValues(bottom = JannahSpacing.xxl)
        ) {
            items(target) { index ->
                val dayNumber = index + 1
                val (start, end) = KhatmStore.getRangeForDay(ctx, index)
                val isCompleted = index < completed
                val isActive = index == completed
                val isLocked = index > completed

                Column {
                    Row(
                        Modifier.fillMaxWidth().pressClickable(enabled = !isLocked) { if (isActive || isCompleted) onOpenRange(start, end) }.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Day $dayNumber", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                            Spacer(Modifier.height(4.dp))
                            Text("Pages $start to $end", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = if (isLocked) FontWeight.Normal else FontWeight.Bold, color = if (isLocked) JannahColors.Muted else JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text("${end - start + 1} pages", style = JannahType.caption, color = JannahColors.Muted)
                        }
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(when { isCompleted -> JannahColors.Orange; isActive -> JannahColors.OrangeSoft; else -> Color.Transparent })
                                .pressClickable(enabled = isActive) { KhatmStore.markDayCompleted(ctx); refresh++ },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> Text("✓", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                isActive -> Text("○", color = JannahColors.Orange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                else -> Box(Modifier.size(6.dp).clip(CircleShape).background(JannahColors.Border))
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }
        }
    }

    pendingReset?.let { newDays ->
        AlertDialog(
            onDismissRequest = { pendingReset = null },
            containerColor = JannahColors.Surface,
            title = { Text("Change pace?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = JannahColors.Ink) },
            text = { Text("You have completed $completed of $target days. Changing pace will reset this progress. Continue?", style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 20.sp) },
            confirmButton = { TextButton(onClick = { KhatmStore.setTarget(ctx, newDays); KhatmStore.reset(ctx); pendingReset = null; refresh++ }) { Text("Change anyway", color = JannahColors.Orange, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { pendingReset = null }) { Text("Cancel", color = JannahColors.Muted) } }
        )
    }
}
