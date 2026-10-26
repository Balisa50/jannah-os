package com.jannahos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun ScreenHeader(
    title: String,
    eyebrow: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(JannahColors.Orange)
            .statusBarsPadding() // Flows seamlessly into the status bar
            .padding(horizontal = JannahSpacing.gutter, vertical = 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(JannahColors.OrangeDeep).clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", fontSize = 18.sp, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Text(title, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(eyebrow.uppercase(), style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
            Spacer(Modifier.height(4.dp))
            Text(subtitle ?: title, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange, lineHeight = 38.sp)
        }
    }
}
