package com.jannahos.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jannahos.MushafImageStore
import com.jannahos.QuranCache
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
    var currentSurah by remember { mutableIntStateOf(1) }

    LaunchedEffect(pagerState.currentPage) {
        val pd = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, currentPage) }
        if (pd != null) currentSurah = pd.ayahs.firstOrNull()?.surah ?: 1
    }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Column(Modifier.fillMaxSize()) {
            val surahTitle = QuranCache.SURAHS.firstOrNull { it.first == currentSurah }?.second ?: "Quran"
            TopBar(title = surahTitle, onBack = onBack)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                MushafImagePage(ctx, pageIndex + 1)
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavPill("←") { if (pagerState.currentPage > 0) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                Text("$currentPage / 604", style = JannahType.caption, color = JannahColors.Muted)
                NavPill("→") { if (pagerState.currentPage < 603) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            }
        }
    }
}

@Composable
private fun MushafImagePage(ctx: android.content.Context, pageNum: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    

    LaunchedEffect(pageNum) {
                bitmap = withContext(Dispatchers.IO) {
            MushafImageStore.loadBitmap(ctx, pageNum)
                ?: run {
                    MushafImageStore.downloadPage(ctx, pageNum)
                    MushafImageStore.loadBitmap(ctx, pageNum)
                }
        }
            }

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFCF3))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page $pageNum",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // Skeleton loader that occupies the exact same space, no layout jump
            Box(Modifier.fillMaxSize().background(Color(0xFFF7F4EF)), contentAlignment = Alignment.Center) {
                Text("Page $pageNum", style = JannahType.caption, color = JannahColors.Muted.copy(alpha = 0.3f))
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
