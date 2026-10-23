package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun KhatmScreen(onBack: () -> Unit, onOpenPage: (Int) -> Unit) {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    val target = KhatmStore.getTarget(ctx)
    val completed = KhatmStore.getCompleted(ctx)
    var daysInput by remember { mutableStateOf(target.toString()) }
    val (start, end) = KhatmStore.getPagesForDay(ctx)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Reading Plan", onBack = onBack)
        
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(JannahSpacing.gutter)) {
            Text("CHOOSE YOUR PACE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = daysInput,
                onValueChange = { daysInput = it },
                label = { Text("Number of days (e.g., 10, 30, 45)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable {
                val days = daysInput.toIntOrNull() ?: 30
                KhatmStore.setTarget(ctx, days)
                KhatmStore.reset(ctx)
                refresh++
            }, contentAlignment = Alignment.Center) {
                Text("Save Plan", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
            Text("TODAY'S PLAN", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("Read pages $start to $end", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(16.dp))
            
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { onOpenPage(start) }, contentAlignment = Alignment.Center) {
                Text("Open Mushaf at Page $start", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
            Text("Progress: $completed of $target days", style = JannahType.bodySmall, color = JannahColors.Muted)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.Border)) {
                Box(Modifier.fillMaxWidth(if (target == 0) 0f else completed.toFloat() / target).height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.Orange))
            }

            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(if (completed >= target) JannahColors.Border else JannahColors.Ink).pressClickable(enabled = completed < target) {
                KhatmStore.markDayCompleted(ctx)
                refresh++
            }, contentAlignment = Alignment.Center) {
                Text(if (completed >= target) "Plan Complete" else "I read these pages", style = JannahType.button, color = JannahColors.OnOrange)
            }
        }
    }
}
