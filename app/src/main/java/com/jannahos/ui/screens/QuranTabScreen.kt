package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jannahos.QuranBookmarks
import com.jannahos.QuranCache
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun QuranTabScreen(
    onNavigateToSurah: (Int) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val last = remember { QuranBookmarks.lastSurah(ctx) }
    val completedDays = remember { KhatmStore.getCompleted(ctx) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        item {
            Column(Modifier.padding(horizontal = JannahSpacing.gutter).padding(top = 64.dp, bottom = 24.dp)) {
                Text("Quran", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(4.dp))
                Text("114 Surahs", style = JannahType.caption, color = JannahColors.Muted)
            }
        }

        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable { onNavigateToSearch() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("Search", style = JannahType.button, color = JannahColors.OnOrange)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Orange).pressClickable { onNavigateToPlan() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text(if (completedDays == 0) "Start Plan" else "Plan: $completedDays", style = JannahType.button, color = JannahColors.OnOrange)
                }
            }
        }

        if (last != null) {
            val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
            item {
                Column(Modifier.padding(horizontal = JannahSpacing.gutter).padding(top = 24.dp)) {
                    Row(Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(last) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue: ", style = JannahType.body, color = JannahColors.Muted)
                        Text(name, style = JannahType.body, color = JannahColors.Orange, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }
        }

        items(QuranCache.SURAHS, key = { it.first }) { surah ->
            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Row(Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(surah.first) }.padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${surah.first}", style = JannahType.caption, color = JannahColors.Faint, modifier = Modifier.width(40.dp))
                    Text(surah.second, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.body, color = JannahColors.Muted)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            }
        }

        item { Spacer(Modifier.height(JannahSpacing.xxl)) }
    }
}
