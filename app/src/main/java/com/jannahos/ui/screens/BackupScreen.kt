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
fun BackupScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Backup & Restore", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).padding(JannahSpacing.l)) {
                Column {
                    Text("BACKUP", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text("Your data, yours.", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                    Spacer(Modifier.height(4.dp))
                    Text("Export everything to a file. Import it anywhere.", style = JannahType.caption, color = JannahColors.OnOrange.copy(alpha = 0.85f))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))
            Text("WHAT IS BACKED UP", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Card).padding(JannahSpacing.l)) {
                Column {
                    val items = listOf(
                        "Prayer & tahajjud history",
                        "Diary reflections and Dua notes",
                        "Private commitments & letters",
                        "Quran bookmarks and last read",
                        "Settings, calculation method, adhan choice"
                    )
                    items.forEach { 
                        Text("· $it", style = JannahType.bodySmall, color = JannahColors.Ink)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Export backup", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Import backup", style = JannahType.button, color = JannahColors.Ink)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.OrangeSoft).padding(JannahSpacing.l)) {
                Column {
                    Text("PRIVACY", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(4.dp))
                    Text("The file is written only where you choose. Nothing is ever uploaded. If you restore on another phone, your new device inherits your data, but nothing leaves your control.", style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 20.sp)
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
