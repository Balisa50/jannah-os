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
    var layout by remember { mutableStateOf<QuranStore.PageLayout?>(null) }
    var loading by remember { mutableStateOf(true) }
    var dragTotal by remember { mutableStateOf(0f) }
    val fontSize = remember { QuranReaderPrefs.fontSize(ctx) }

    LaunchedEffect(currentPage) {
        loading = true
        layout = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, currentPage) }
        loading = false
    }

    val surahName = remember(layout) {
        val s = layout?.firstSurah ?: 1
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
            } else if (layout == null || layout!!.lines.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(JannahSpacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Page not downloaded yet.",
                        style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else {
                val l = layout!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.m)
                ) {
                    // Top row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الجزء ${juzForPage(currentPage)}",
                            fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
                        Text(surahName,
                            fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0D5B8)))
                    Spacer(Modifier.height(JannahSpacing.s))

                    // 15-line grid
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        l.lines.toSortedMap().forEach { (lineNo, text) ->
                            // Basmala is centered
                            if (lineNo == l.basmalaLineNumber) {
                                Text(
                                    text,
                                    fontFamily = UthmanicHafs,
                                    fontSize = (fontSize + 2).sp,
                                    color = JannahColors.Ink,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = (fontSize * 2f).sp
                                )
                            } else {
                                // Regular line — center for short lines, justify for long
                                Text(
                                    text,
                                    fontFamily = UthmanicHafs,
                                    fontSize = fontSize.sp,
                                    color = JannahColors.Ink,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = (fontSize * 2f).sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.m))
                    Text("$currentPage",
                        fontFamily = UthmanicHafs, fontSize = 13.sp, color = JannahColors.OrangeDeep,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Nav row
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

private fun juzForPage(page: Int): Int {
    val juzStarts = intArrayOf(
        1, 22, 42, 62, 82, 102, 121, 142, 162, 181,
        201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
        402, 422, 442, 462, 482, 502, 522, 542, 562, 582
    )
    for (i in juzStarts.indices.reversed()) if (page >= juzStarts[i]) return i + 1
    return 1
}
