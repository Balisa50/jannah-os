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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(JannahColors.Cream)
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = com.jannahos.R.drawable.quran_hero),
                    contentDescription = "Quran",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                    )
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = JannahSpacing.gutter)
                        .padding(bottom = JannahSpacing.m)
                ) {
                    Text("QURAN", style = JannahType.eyebrow, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(4.dp))
                    Text("114 Surahs", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(top = JannahSpacing.m),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Card)
                        .pressClickable { onNavigateToSearch() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(50)).background(JannahColors.Card)
                        .pressClickable { onNavigateToPlan() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (completedDays == 0) "Start Plan" else "Plan: $completedDays",
                        style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (last != null) {
            val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
            item {
                Column(Modifier.padding(horizontal = JannahSpacing.gutter).padding(top = JannahSpacing.l)) {
                    Row(
                        Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(last) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Continue reading: ", style = JannahType.bodySmall, color = JannahColors.Muted)
                        Text(name, style = JannahType.bodySmall, color = JannahColors.Orange, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                    Spacer(Modifier.height(JannahSpacing.s))
                }
            }
        } else {
            item { Spacer(Modifier.height(JannahSpacing.l)) }
        }

        items(QuranCache.SURAHS, key = { it.first }) { surah ->
            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Row(
                    Modifier.fillMaxWidth().pressClickable { onNavigateToSurah(surah.first) }.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${surah.first}", style = JannahType.caption, color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(surah.second, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
        }

        item { Spacer(Modifier.height(JannahSpacing.xxl)) }
    }
}
