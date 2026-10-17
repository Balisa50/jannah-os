package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranReaderPrefs
import com.jannahos.QuranStore
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.pressClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))

@Composable
fun MushafPageScreen(startPage: Int = 1, onBack: () -> Unit) {
    SwipeBackScreen(onBack = onBack) {
        MushafPageContent(startPage = startPage, onBack = onBack)
    }
}

@Composable
private fun MushafPageContent(startPage: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = startPage.coerceIn(1, QuranStore.totalPages()) - 1,
        pageCount = { QuranStore.totalPages() }
    )
    val fontSize = remember { QuranReaderPrefs.fontSize(ctx) }

    val currentPage = pagerState.currentPage + 1
    var currentSurahName by remember { mutableStateOf("Quran") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val l = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, currentPage) }
        if (l != null) {
            val s = l.firstSurah
            currentSurahName = com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == s }?.second ?: "Surah $s"
        }
    }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = currentSurahName, onBack = onBack)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val pageNum = pageIndex + 1
                MushafPageItem(ctx, pageNum, fontSize)
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavPill("←") { if (pagerState.currentPage > 0) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                Text("$currentPage / 604", style = JannahType.caption, color = JannahColors.Muted)
                NavPill("→") { if (pagerState.currentPage < QuranStore.totalPages() - 1) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            }
        }

        // Audio FAB (Ready to be wired to MediaPlayer/Service)
        FloatingActionButton(
            onClick = { /* TODO: Wire Audio */ },
            containerColor = JannahColors.Orange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio")
        }
    }
}

@Composable
private fun MushafPageItem(ctx: android.content.Context, pageNum: Int, fontSize: Int) {
    var layout by remember { mutableStateOf<QuranStore.PageLayout?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pageNum) {
        loading = true
        layout = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, pageNum) }
        loading = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFCF3))
            .border(1.dp, Color(0xFFE0D5B8), RoundedCornerShape(12.dp))
    ) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JannahColors.Orange)
            }
        } else if (layout == null || layout!!.lines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Page not downloaded.", style = JannahType.bodySmall, color = JannahColors.Muted)
            }
        } else {
            val l = layout!!
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.m)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الجزء ${juzForPage(pageNum)}", fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
                }
                Spacer(Modifier.height(JannahSpacing.s))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0D5B8)))
                Spacer(Modifier.height(JannahSpacing.m))

                l.lines.toSortedMap().forEach { (lineNo, text) ->
                    val isBasmala = lineNo == l.basmalaLineNumber
                    val isHeader = lineNo == l.surahHeaderLineNumber

                    Text(
                        text = text,
                        fontFamily = UthmanicHafs,
                        fontSize = fontSize.sp,
                        color = JannahColors.Ink,
                        textAlign = if (isBasmala || isHeader) TextAlign.Center else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(vertical = if (isBasmala) 12.dp else 4.dp),
                        lineHeight = (fontSize * 1.8f).sp
                    )
                }

                Spacer(Modifier.height(JannahSpacing.l))
                Text("$pageNum", fontFamily = UthmanicHafs, fontSize = 14.sp, color = JannahColors.OrangeDeep, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(JannahSpacing.l))
            }
        }
    }
}

@Composable
private fun NavPill(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Card)
            .pressClickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(label, style = JannahType.title, color = JannahColors.Ink)
    }
}

private fun juzForPage(page: Int): Int {
    val juzStarts = intArrayOf(1, 22, 42, 62, 82, 102, 121, 142, 162, 181, 201, 222, 242, 262, 282, 302, 322, 342, 362, 382, 402, 422, 442, 462, 482, 502, 522, 542, 562, 582)
    for (i in juzStarts.indices.reversed()) if (page >= juzStarts[i]) return i + 1
    return 1
}
