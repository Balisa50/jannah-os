package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
fun SunnahScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Sunnah prayers", onBack = onBack)
        Column(Modifier.padding(horizontal = JannahSpacing.gutter).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(JannahSpacing.m))
            
            val sunnahs = listOf(
                Triple("4r", "Dhuhr · 4 before", null),
                Triple("2r", "Dhuhr · 2 after", null),
                Triple("2r", "Maghrib · 2 after", null),
                Triple("2r", "Isha · 2 after", null),
                Triple("1r", "Witr · odd night prayer", "The Prophet ﷺ said: \"Allah is Witr and He loves witr.\""),
                Triple("2r", "Duha · forenoon", "Recommended between sunrise and Dhuhr.")
            )
            
            sunnahs.forEach { (rakahs, title, desc) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(JannahColors.Card), contentAlignment = Alignment.Center) {
                        Text(rakahs, fontSize = 12.sp, color = JannahColors.Ink, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        if (desc != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(desc, style = JannahType.caption, color = JannahColors.Muted)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
            
            Spacer(Modifier.height(JannahSpacing.xl))
            Box(Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                Column {
                    Text("EVIDENCE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(4.dp))
                    Text("Umm Habibah reported that the Prophet ﷺ said: \"Whoever prays twelve rak'ah in a day and night, a house will be built for him in Paradise.\" (Sahih Muslim 728).", style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 22.sp)
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
