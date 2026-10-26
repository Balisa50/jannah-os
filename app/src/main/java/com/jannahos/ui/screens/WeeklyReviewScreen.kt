package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun WeeklyReviewScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Focus Setup", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).padding(JannahSpacing.l)) {
                Column {
                    Text("SYSTEM ACTIVE", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text("The app is working in the background.", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange, lineHeight = 28.sp)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("WHAT THE SYSTEM IS DOING", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            FocusRow("Prayer Locks", "Enforcing screen locks during Fard times.")
            FocusRow("Quran Lock", "Enforcing the daily reading session.")
            FocusRow("Adhan Alarms", "Firing at exact prayer times with escalating alerts.")
            FocusRow("DND (Sanctuary)", "Silencing notifications when locks are active.")

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("THE GOAL", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "The point of this app is to be closed. The locks and alarms are designed to pull you away from the phone and towards prayer and reading. If you don't open the app for a week, but the locks fired and the alarms rang, it succeeded.",
                style = JannahType.body, color = JannahColors.Body, lineHeight = 24.sp
            )

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun FocusRow(title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(JannahColors.Orange).padding(top = 6.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(desc, style = JannahType.caption, color = JannahColors.Muted)
        }
    }
}
