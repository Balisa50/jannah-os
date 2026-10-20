package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.jannahos.HifdhStore
import com.jannahos.QuranCache
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun HifdhScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var memorized by remember { mutableStateOf(HifdhStore.getMemorized(ctx)) }
    val progress = if (memorized.isEmpty()) 0f else memorized.size / 114f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar("Hifdh Tracker", onBack = onBack)

        // Header with progress
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)
        ) {
            Column {
                Text("MEMORIZATION", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${memorized.size}", fontFamily = FontFamily.Serif, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.width(8.dp))
                    Text("of 114 Surahs", style = JannahType.body, color = JannahColors.Muted, modifier = Modifier.padding(bottom = 8.dp))
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(JannahColors.Border)
                ) {
                    Box(
                        Modifier.fillMaxWidth(progress).height(6.dp).clip(CircleShape).background(JannahColors.Orange)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)
        ) {
            items(QuranCache.SURAHS.size) { index ->
                val surah = QuranCache.SURAHS[index]
                val isMemorized = memorized.contains(surah.first)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable {
                            HifdhStore.setMemorized(ctx, surah.first, !isMemorized)
                            memorized = HifdhStore.getMemorized(ctx)
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isMemorized) JannahColors.Orange else JannahColors.Card)
                    ) {
                        if (isMemorized) {
                            Text("✓", color = JannahColors.OnOrange, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(surah.second, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        Text("Surah ${surah.first}", style = JannahType.micro, color = JannahColors.Muted)
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
        }
    }
}
