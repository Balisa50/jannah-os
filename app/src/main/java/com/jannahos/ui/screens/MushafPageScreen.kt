package com.jannahos.ui.screens

import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.MushafImageStore
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.pressClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

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
        initialPage = startPage.coerceIn(1, 604) - 1,
        pageCount = { 604 }
    )
    val currentPage = pagerState.currentPage + 1
    val scope = rememberCoroutineScope()
    var isAudioPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFFFFCF3))) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp,
            beyondViewportPageCount = 2
        ) { pageIndex ->
            MushafImagePage(ctx, pageIndex + 1)
        }

        // Floating Top Bar
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(JannahColors.Cream.copy(alpha = 0.95f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.padding(start = 16.dp).size(40.dp).clip(CircleShape).background(JannahColors.Card).pressClickable { onBack() },
                    contentAlignment = Alignment.Center
                ) { Text("←", fontSize = 20.sp) }
                Spacer(Modifier.width(16.dp))
                Text("Quran", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            }
        }

        // Floating Bottom Controls
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(JannahColors.Cream.copy(alpha = 0.95f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPill("←") { if (pagerState.currentPage > 0) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
            
            Box(
                Modifier.clip(CircleShape).background(JannahColors.Orange).pressClickable {
                    if (isAudioPlaying) {
                        mediaPlayer?.pause()
                        isAudioPlaying = false
                    } else {
                        if (mediaPlayer == null) {
                            val surah = getSurahForPage(currentPage)
                            val url = "https://server8.mp3quran.net/afs/${String.format("%03d", surah)}.mp3"
                            val mp = MediaPlayer()
                            mediaPlayer = mp
                            mp.setOnPreparedListener { it.start(); isAudioPlaying = true }
                            mp.setOnCompletionListener { isAudioPlaying = false }
                            mp.setOnErrorListener { _, _, _ -> isAudioPlaying = false; true }
                            scope.launch(Dispatchers.IO) {
                                try { mp.setDataSource(url); mp.prepareAsync() } catch (e: Exception) {}
                            }
                        } else {
                            mediaPlayer?.start()
                            isAudioPlaying = true
                        }
                    }
                }.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isAudioPlaying) "❚❚" else "▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Text("$currentPage / 604", style = JannahType.caption, color = JannahColors.Muted)
            NavPill("→") { if (pagerState.currentPage < 603) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
        }
    }
}

@Composable
private fun MushafImagePage(ctx: android.content.Context, pageNum: Int) {
    val bitmap = remember(pageNum) { MushafImageStore.loadBitmap(ctx, pageNum) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Page $pageNum",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", style = JannahType.caption, color = JannahColors.Muted)
        }
    }
}

@Composable
private fun NavPill(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(JannahColors.Card)
            .pressClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, style = JannahType.title, color = JannahColors.Ink)
    }
}

private fun getSurahForPage(page: Int): Int {
    val pages = intArrayOf(1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267, 282, 293, 305, 312, 322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415, 418, 428, 434, 440, 446, 453, 458, 467, 477, 483, 489, 496, 499, 502, 507, 511, 515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549, 551, 553, 554, 556, 558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585, 586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597, 598, 598, 599, 599, 600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604, 604, 604)
    for (i in pages.indices.reversed()) { if (page >= pages[i]) return i + 1 }
    return 1
}
