package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    LaunchedEffect(currentPage) {
        loading = true
        verses = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, currentPage) ?: emptyList() }
        loading = false
    }

    val surahName = remember(verses) {
        val s = verses.firstOrNull()?.surah ?: 1
        com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == s }?.second ?: "Surah $s"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar(title = surahName, onBack = onBack)

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFFFCF3))
                .border(1.dp, Color(0xFFE0D5B8), RoundedCornerShape(6.dp))
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
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading", style = JannahType.caption, color = JannahColors.Muted)
                }
            } else if (verses.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(JannahSpacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Page not downloaded yet.",
                        style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.m)
                ) {
                    // Surah header if a new surah begins on this page
                    val firstVerse = verses.firstOrNull()
                    if (firstVerse != null && firstVerse.ayah == 1 && firstVerse.surah != 1 && firstVerse.surah != 9) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF6EEDB))
                                .border(1.dp, Color(0xFFC9A961), RoundedCornerShape(3.dp))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "سورة $surahName",
                                fontFamily = UthmanicHafs,
                                fontSize = 18.sp,
                                color = JannahColors.Ink
                            )
                        }
                        Spacer(Modifier.height(JannahSpacing.m))
                    }

                    // Flowing mushaf text, justified, RTL
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
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(JannahSpacing.l))

                    Text(
                        "$currentPage",
                        fontFamily = UthmanicHafs,
                        fontSize = 13.sp,
                        color = JannahColors.OrangeDeep,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPill("←") { if (currentPage > 1) currentPage-- }
            Text("$currentPage / 604",
                style = JannahType.caption, color = JannahColors.Muted)
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
            .pointerInput(Unit) { detectHorizontalDragGestures { _, _ -> } }
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(label, style = JannahType.title, color = JannahColors.Ink)
    }
}

private fun toArabicIndic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
