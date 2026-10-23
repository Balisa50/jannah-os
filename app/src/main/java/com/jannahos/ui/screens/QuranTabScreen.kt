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
import com.jannahos.QuranBookmarks
import com.jannahos.QuranCache
import com.jannahos.QuranStore
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun QuranTabScreen(
    onNavigateToSurah: (Int) -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    var showKhatm by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    if (showKhatm) { 
        KhatmScreen(onBack = { showKhatm = false }, onOpenPage = { page -> onNavigateToSurah(page) })
        return 
    }
    if (showSearch) { 
        QuranSearchScreen(onBack = { showSearch = false }, onNavigateToPage = { page -> onNavigateToSurah(page) })
        return 
    }

    val ctx = LocalContext.current
    val last = remember { QuranBookmarks.lastSurah(ctx) }
    val completedDays = KhatmStore.getCompleted(ctx)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.jannahos.R.drawable.quran_hero),
                contentDescription = "Quran",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.m)) {
                Text("QURAN", style = JannahType.eyebrow, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text("114 Surahs", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.m))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable { showSearch = true }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Search", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Card).pressClickable { showKhatm = true }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(if (completedDays == 0) "Start Plan" else "Plan: $completedDays", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            if (last != null) {
                val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                Row(Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(last) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Continue reading: ", style = JannahType.bodySmall, color = JannahColors.Muted)
                    Text(name, style = JannahType.bodySmall, color = JannahColors.Orange, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                Spacer(Modifier.height(JannahSpacing.s))
            }

            QuranCache.SURAHS.forEachIndexed { i, (num, name) ->
                Row(Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(num) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$num", style = JannahType.caption, color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(name, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < QuranCache.SURAHS.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
