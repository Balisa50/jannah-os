package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.util.Calendar

data class Dhikr(val arabic: String, val translation: String, val target: Int)

@Composable
fun AdhkarScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptics = LocalHapticFeedback.current
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isMorning = hour in 0..14

    val adhkar = if (isMorning) listOf(
        Dhikr("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ", "We have entered the morning and the dominion belongs to Allah.", 1),
        Dhikr("اللَّهُمَّ بِكَ أَصْبَحْنَا", "O Allah, by You we enter the morning.", 1),
        Dhikr("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Glory be to Allah and praise Him.", 100)
    ) else listOf(
        Dhikr("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ", "We have entered the evening and the dominion belongs to Allah.", 1),
        Dhikr("اللَّهُمَّ بِكَ أَمْسَيْنَا", "O Allah, by You we enter the evening.", 1),
        Dhikr("أَسْتَغْفِرُ اللَّهَ", "I seek forgiveness from Allah.", 100)
    )

    var counts by remember { mutableStateOf(adhkar.map { 0 }) }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(
            title = if (isMorning) "Morning Adhkar" else "Evening Adhkar",
            eyebrow = "DAILY",
            subtitle = "Protect your day.",
            onBack = onBack
        )

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            adhkar.forEachIndexed { index, dhikr ->
                val count = counts[index]
                val isDone = count >= dhikr.target

                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Text(dhikr.arabic, fontFamily = FontFamily.Serif, fontSize = 28.sp, color = JannahColors.Ink, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Text(dhikr.translation, style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${dhikr.target} times", style = JannahType.caption, color = JannahColors.OrangeDeep)
                        Box(
                            Modifier.size(60.dp).clip(CircleShape)
                                .background(if (isDone) JannahColors.Orange else JannahColors.Card)
                                .pressClickable(enabled = !isDone) {
                                    counts = counts.toMutableList().also { it[index] = count + 1 }
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isDone) "✓" else "$count", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isDone) JannahColors.OnOrange else JannahColors.Ink)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
