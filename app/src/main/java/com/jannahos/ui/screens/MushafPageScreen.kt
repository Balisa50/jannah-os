package com.jannahos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType

@Composable
fun MushafPageScreen(startPage: Int, endPage: Int = 604, onBack: () -> Unit) {
    SwipeBackScreen(onBack = onBack) {
        val ctx = LocalContext.current
        val pageCount = endPage - startPage + 1
        
        val pagerState = rememberPagerState(
            initialPage = 0, // 0 index maps to startPage
            pageCount = { pageCount }
        )
        
        val currentRealPage = startPage + pagerState.currentPage
        val scope = rememberCoroutineScope()

        Box(Modifier.fillMaxSize().background(Color(0xFFFFFCF3))) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val realPageNum = startPage + pageIndex
                val bitmap = remember(realPageNum) { MushafImageStore.loadBitmap(ctx, realPageNum) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Page $realPageNum",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Top Bar
            Row(
                Modifier.fillMaxWidth().background(JannahColors.Cream.copy(alpha = 0.95f)).windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(JannahColors.Card).pressClickable { onBack() }, contentAlignment = Alignment.Center) {
                    Text("←", fontSize = 20.sp, color = JannahColors.Ink)
                }
                Spacer(Modifier.width(16.dp))
                Text("Quran", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            }

            // Bottom Counter
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(JannahColors.Cream.copy(alpha = 0.95f)).windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Page ${currentRealPage} of $endPage (${pagerState.currentPage + 1} / $pageCount)", 
                    style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
