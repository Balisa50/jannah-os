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
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun WeeklyReviewScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Focus controls", eyebrow = "DEVICE AUTOMATIONS", subtitle = "Review the optional controls and permissions behind them.", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).padding(JannahSpacing.l)) {
                Column {
                    Text("CONTROLLED BY YOU", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text("Optional tools only run when enabled and permitted.", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange, lineHeight = 28.sp)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("WHAT EACH CONTROL NEEDS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            FocusRow("Prayer Lock", "Optional app blocking during prayer. Requires the relevant Accessibility permission.")
            FocusRow("Quran Lock", "A scheduled reading control. Check its schedule and permissions in App Settings.")
            FocusRow("Adhan reminders", "Android notifications and exact-alarm access can affect delivery.")
            FocusRow("Sanctuary", "Do Not Disturb changes only during an active focus session, when permission is granted.")
            FocusRow("Bedtime controls", "Use App Settings to review the bedtime trigger and its current configuration.")

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("NO HIDDEN CONTROL", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "SIRAJ does not silently enable these controls. Each feature has its own setting and may depend on Android permissions, manufacturer battery rules and your device configuration. Review them before relying on an alarm or lock.",
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
