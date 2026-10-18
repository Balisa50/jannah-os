package com.jannahos.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val scope = rememberCoroutineScope()
    var currentSurah by remember { mutableIntStateOf(1) }

    LaunchedEffect(pagerState.currentPage) {
        val pd = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, currentPage) }
        if (pd != null) currentSurah = pd.ayahs.firstOrNull()?.surah ?: 1
    }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Column(Modifier.fillMaxSize()) {
            val surahTitle = com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == currentSurah }?.second ?: "Quran"
            TopBar(title = surahTitle, onBack = onBack)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                MushafPageItem(ctx, pageIndex + 1, fontSize)
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

        AudioFab(surah = currentSurah, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 80.dp).size(56.dp))
    }
}

@Composable
private fun AudioFab(surah: Int, modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(surah) {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    FloatingActionButton(
        onClick = {
            if (isLoading) return@FloatingActionButton
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                if (mediaPlayer == null) {
                    isLoading = true
                    val url = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surah.mp3"
                    val mp = MediaPlayer()
                    mediaPlayer = mp
                    mp.setOnPreparedListener {
                        it.start()
                        isPlaying = true
                        isLoading = false
                    }
                    mp.setOnErrorListener { _, _, _ ->
                        isLoading = false
                        isPlaying = false
                        true
                    }
                    mp.setOnCompletionListener { isPlaying = false }
                    
                    scope.launch(Dispatchers.IO) {
                        try {
                            mp.setDataSource(url)
                            mp.prepareAsync()
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { isLoading = false }
                        }
                    }
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                }
            }
        },
        containerColor = JannahColors.Orange,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(if (isPlaying) "❚❚" else "▶", fontSize = 20.sp, color = Color.White)
        }
    }
}

@Composable
private fun MushafPageItem(ctx: android.content.Context, pageNum: Int, fontSize: Int) {
    var pageData by remember { mutableStateOf<QuranStore.PageData?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pageNum) {
        loading = true
        pageData = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, pageNum) }
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
        } else if (pageData == null || pageData!!.ayahs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Page not downloaded.", style = JannahType.bodySmall, color = JannahColors.Muted)
            }
        } else {
            val pd = pageData!!
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.m)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الجزء ${juzForPage(pageNum)}", fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
                    Text(pd.surahNames[pd.ayahs.first().surah] ?: "", fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
                }
                Spacer(Modifier.height(JannahSpacing.s))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0D5B8)))
                Spacer(Modifier.height(JannahSpacing.m))

                pd.ayahs.forEach { ayah ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = ayah.text,
                            fontFamily = UthmanicHafs,
                            fontSize = fontSize.sp,
                            color = JannahColors.Ink,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.weight(1f),
                            lineHeight = (fontSize * 1.8f).sp
                        )
                        Box(
                            Modifier
                                .padding(start = 6.dp, top = 4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(1.dp, JannahColors.OrangeDeep, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ayah.ayahInSurah.toString(),
                                fontSize = 9.sp,
                                color = JannahColors.OrangeDeep,
                                fontFamily = UthmanicHafs
                            )
                        }
                    }
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
