package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.jannahos.QuranStore
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
    val (start, end) = KhatmStore.getPagesForDay(ctx)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Reading Plan", onBack = onBack)
        
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(JannahSpacing.gutter)) {
            
            Text("PREVIEW · FIRST 3 DAYS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(12.dp))

            // Day 1
            DayCard("Day 1", start, end) { onOpenRange(start, end) }
            Spacer(Modifier.height(12.dp))

            // Day 2
            val day2Start = end + 1
            val day2End = minOf(day2Start + (end - start), 604)
            DayCard("Day 2", day2Start, day2End) { onOpenRange(day2Start, day2End) }
            Spacer(Modifier.height(12.dp))

            // Day 3
            val day3Start = day2End + 1
            val day3End = minOf(day3Start + (end - start), 604)
            DayCard("Day 3", day3Start, day3End) { onOpenRange(day3Start, day3End) }
            
            Spacer(Modifier.height(32.dp))
            Text("Progress: $completed of $target days completed", style = JannahType.bodySmall, color = JannahColors.Muted)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.Border)) {
                Box(Modifier.fillMaxWidth(if (target == 0) 0f else completed.toFloat() / target).height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.Orange))
            }

            Spacer(Modifier.height(32.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(if (completed >= target) JannahColors.Border else JannahColors.Ink)
                    .pressClickable(enabled = completed < target) {
                        KhatmStore.markDayCompleted(ctx)
                        refresh++
                    }, 
                contentAlignment = Alignment.Center
            ) {
                Text(if (completed >= target) "Plan Complete" else "I read today's pages", style = JannahType.button, color = JannahColors.OnOrange)
            }
        }
    }
}

@Composable
private fun DayCard(dayText: String, start: Int, end: Int, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Card).pressClickable { onClick() }.padding(16.dp)) {
        Column {
            Text(dayText, style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Pages $start to $end", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text("${end - start + 1} pages", style = JannahType.caption, color = JannahColors.Muted)
        }
    }
}
