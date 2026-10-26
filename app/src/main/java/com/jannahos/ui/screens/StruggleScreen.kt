package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun StruggleScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Leave it, gently", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).padding(JannahSpacing.l)) {
                Column {
                    Text("PRIVATE", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text("Stays on your phone.", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink), contentAlignment = Alignment.Center) {
                Text("+ Add something to leave", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Card).padding(JannahSpacing.l)) {
                Column {
                    Text("A quiet place", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("This space is for habits you want to leave. Name them however you like. Track them only for yourself. No public list, no leaderboard, no exposure.", style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
