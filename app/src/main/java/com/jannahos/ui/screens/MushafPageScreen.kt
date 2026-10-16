package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranReaderPrefs
import com.jannahos.QuranStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// KFGQPC Uthmanic Hafs — the exact typeface used in the printed Madinah mushaf.
private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))

@Composable
fun MushafPageScreen(startPage: Int = 1, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    var currentPage by remember { mutableIntStateOf(startPage.coerceIn(1, QuranStore.totalPages())) }
    var verses by remember { mutableStateOf<List<QuranStore.PageVerse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var dragTotal by remember { mutableStateOf(0f) }
    var fontSize by remember { mutableIntStateOf(QuranReaderPrefs.fontSize(ctx)) }

    val surahNameForPage = remember(currentPage) { surahOfPage(verses) }

    LaunchedEffect(currentPage) {
        loading = true
        verses = withContext(Dispatchers.IO) {
            QuranStore.loadPage(ctx, currentPage) ?: emptyList()
        }
        QuranReaderPrefs.setLastSurah(ctx, verses.firstOrNull()?.surah ?: 1)
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar(
            title = if (surahNameForPage.isBlank()) "Page $currentPage" else surahNameForPage,
            onBack = onBack
        )

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFFCF5))
                .pointerInput(currentPage) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragTotal < -90f && currentPage < QuranStore.totalPages()) currentPage++
                            else if (dragTotal > 90f && currentPage > 1) currentPage--
                            dragTotal = 0f
                        },
                        onHorizontalDrag = { _, d -> dragTotal += d }
                    )
                }
        ) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading", style = JannahType.caption, color = JannahColors.Muted)
                }
                verses.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(JannahSpacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Page not downloaded yet.",
                        style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.l)
                    ) {
                        // Basmalah only when a surah begins on this page
                        if (verses.firstOrNull()?.ayah == 1 &&
                            verses.firstOrNull()?.surah != 1 &&
                            verses.firstOrNull()?.surah != 9) {
                            Text(
                                "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                fontFamily = UthmanicHafs,
                                fontSize = (fontSize + 4).sp,
                                color = JannahColors.Ink,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = (fontSize * 2.1f).sp
                            )
                            Spacer(Modifier.height(JannahSpacing.m))
                        }

                        // The page text — one flowing paragraph, mushaf style.
                        Text(
                            text = buildString {
                                verses.forEach { v ->
                                    append(v.arabic.trim())
                                    append(" ۝")
                                    append(toArabicIndic(v.ayah))
                                    append(" ")
                                }
                            },
                            fontFamily = UthmanicHafs,
                            fontSize = fontSize.sp,
                            color = JannahColors.Ink,
                            lineHeight = (fontSize * 2.1f).sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom row — page controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPill("←") { if (currentPage > 1) currentPage-- }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$currentPage / ${QuranStore.totalPages()}",
                    style = JannahType.caption, color = JannahColors.Muted)
                Text(
                    "A+  A−",
                    style = JannahType.micro,
                    color = JannahColors.Faint,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            NavPill("→") { if (currentPage < QuranStore.totalPages()) currentPage++ }
        }
    }
}

@Composable
private fun NavPill(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Card)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, _ -> }
            }
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(label, style = JannahType.title, color = JannahColors.Ink)
    }
}

private fun surahOfPage(verses: List<QuranStore.PageVerse>): String {
    val s = verses.firstOrNull()?.surah ?: return ""
    return com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == s }?.second ?: "Surah $s"
}

private fun toArabicIndic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
