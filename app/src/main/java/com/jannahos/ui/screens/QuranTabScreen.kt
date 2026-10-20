package com.jannahos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.KhatmPlan
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
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {}
) {
    var showKhatm by remember { mutableStateOf(false) }

    if (showKhatm) { KhatmScreen(onBack = { showKhatm = false }); return }

    SurahList(
        onPick = { onNavigateToSurah(it) },
        onOpenBookmarks = onNavigateToBookmarks,
        onOpenKhatm = { showKhatm = true },
        onOpenSearch = onNavigateToSearch
    )
}

@Composable
private fun SurahList(
    onPick: (Int) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val ctx = LocalContext.current
    val last = remember { QuranBookmarks.lastSurah(ctx) }
    val bookmarkCount = remember { com.jannahos.SafeStore.quranBookmarks(ctx).size }
    val planDays = KhatmPlan.planDays(ctx)
    val planDone = KhatmPlan.completedDays(ctx).size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // HERO HEADER
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            Image(
                painter = painterResource(id = com.jannahos.R.drawable.quran_hero),
                contentDescription = "Quran",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = JannahSpacing.gutter)
                    .padding(bottom = JannahSpacing.m)
            ) {
                Text("QURAN",
                    style = JannahType.eyebrow,
                    color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text("114 Surahs",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.m))

            // Compact Action Row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(JannahColors.Card)
                        .pressClickable { onOpenSearch() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                }
                
                // Plan
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(JannahColors.Card)
                        .pressClickable { onOpenKhatm() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (planDays == 0) "Start Plan" else "Plan: $planDone/$planDays", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                }

                // Bookmarks
                if (bookmarkCount > 0) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(JannahColors.Card)
                            .pressClickable { onOpenBookmarks() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Saved ($bookmarkCount)", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Continue Reading (Only if exists)
            if (last != null) {
                val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable { onPick(last) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Continue reading: ", style = JannahType.bodySmall, color = JannahColors.Muted)
                    Text(name, style = JannahType.bodySmall, color = JannahColors.Orange, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                Spacer(Modifier.height(JannahSpacing.s))
            }

            // Surah List
            QuranCache.SURAHS.forEachIndexed { i, (num, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable { onPick(num) }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$num", style = JannahType.caption,
                        color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < QuranCache.SURAHS.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}


@Composable
fun BookmarksScreen(onBack: () -> Unit, onOpen: (Int, Int) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { com.jannahos.QuranBookmarkStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val marks = store.all()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("SAVED", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text("Bookmarks", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
        }
        
        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            if (marks.isEmpty()) {
                Text("No bookmarks yet. Tap ☆ on any verse to save it here.", style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp)
            } else {
                marks.forEachIndexed { i, m ->
                    val surahName = com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == m.surah }?.second ?: "Surah ${m.surah}"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .pressClickable { onOpen(m.surah, m.ayah) }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("$surahName · ${m.surah}:${m.ayah}", fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                            if (!m.note.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(m.note, style = JannahType.caption, color = JannahColors.Muted)
                            }
                        }
                        Text("×", style = JannahType.body, color = JannahColors.Muted, modifier = Modifier.pressClickable { store.remove(m.surah, m.ayah); refresh++ }.padding(8.dp))
                    }
                    if (i < marks.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                }
            }
            Spacer(Modifier.height(JannahSpacing.l))
            Box(Modifier.clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable(onClick = onBack).padding(horizontal = 24.dp, vertical = 14.dp)) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
