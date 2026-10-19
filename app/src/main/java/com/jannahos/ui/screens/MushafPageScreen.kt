package com.jannahos.ui.screens

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
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.launch

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

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        
        // 1. SLIM TOP HEADER
        Row(
            Modifier
                .fillMaxWidth()
                .background(JannahColors.Cream)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 6.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(JannahColors.Card).pressClickable { onBack() }, // Smaller button
                contentAlignment = Alignment.Center
            ) { Text("←", fontSize = 16.sp, color = JannahColors.Ink) }
            Spacer(Modifier.width(12.dp))
            Text("Quran", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink) // Smaller text
        }

        // 2. PURE WHITE EDGE-TO-EDGE READING AREA
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White), // Pure white background matching the page
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val pageNum = pageIndex + 1
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
        }

        // 3. SLIM BOTTOM FOOTER
        Row(
            Modifier
                .fillMaxWidth()
                .background(JannahColors.Cream)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 4.dp), // Reduced vertical padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPill("←") { if (pagerState.currentPage > 0) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
            
            Text("$currentPage / 604", style = JannahType.bodySmall, color = JannahColors.Ink, fontWeight = FontWeight.Bold) // Smaller, cleaner text
            
            NavPill("→") { if (pagerState.currentPage < 603) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
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
            .padding(horizontal = 16.dp, vertical = 6.dp) // Smaller buttons
    ) {
        Text(label, style = JannahType.bodySmall, color = JannahColors.Ink)
    }
}
