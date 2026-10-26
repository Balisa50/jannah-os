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
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun BackupScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Backup", eyebrow = "EXPORT", subtitle = "Your data, yours.", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            
            Text("WHAT IS BACKED UP", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            val items = listOf(
                "Prayer & tahajjud history",
                "Diary reflections and Dua notes",
                "Private commitments & letters",
                "Quran bookmarks and last read",
                "Settings, calculation method, adhan choice"
            )
            items.forEachIndexed { i, item ->
                Text(item, style = JannahType.body, color = JannahColors.Ink, modifier = Modifier.padding(vertical = 12.dp))
                if (i < items.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink), contentAlignment = Alignment.Center) {
                Text("Export backup", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card), contentAlignment = Alignment.Center) {
                Text("Import backup", style = JannahType.button, color = JannahColors.Ink)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
            Text("PRIVACY", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Text("The file is written only where you choose. Nothing is ever uploaded.", style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp)
            
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
