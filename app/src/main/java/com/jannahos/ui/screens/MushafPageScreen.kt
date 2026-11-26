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
import com.jannahos.KhatmStore
import com.jannahos.MushafImageStore
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun MushafPageScreen(startPage: Int, endPage: Int = 604, isKhatm: Boolean = false, onBack: () -> Unit) {
    SwipeBackScreen(onBack = onBack) {
        val ctx = LocalContext.current
        val pageCount = endPage - startPage + 1

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { pageCount }
        )

        val currentRealPage = startPage + pagerState.currentPage
        
        // Overlay state for the Stopping Guide
        var showStopOverlay by remember { mutableStateOf(false) }

        // Trigger the overlay when reaching the end of the Khatm portion
        LaunchedEffect(pagerState.currentPage) {
            if (isKhatm && pagerState.currentPage == pageCount - 1) {
                showStopOverlay = true
            }
        }

        Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
            Column(Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(JannahColors.Surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(JannahColors.OrangeSoft).pressClickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", fontSize = 16.sp, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Quran", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    
                    Spacer(Modifier.weight(1f))
                    
                    if (isKhatm) {
                        Text("TODAY'S PORTION", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                    }
                }

                // Pager
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFFFCF3))
                ) {
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
                        } else {
                            Column(
                                Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Page unavailable", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                                Spacer(Modifier.height(8.dp))
                                Text("Page $realPageNum could not be loaded from the offline Quran pages.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }

                // Bottom Bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(JannahColors.Surface)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isKhatm) "Page $currentRealPage of $endPage" else "Page $currentRealPage of $endPage (${pagerState.currentPage + 1} / $pageCount)",
                        style = JannahType.micro, color = JannahColors.Muted, fontWeight = FontWeight.Bold
                    )
                }
            }

            // THE STOPPING GUIDE OVERLAY
            if (showStopOverlay) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).pressClickable { },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(JannahColors.Surface)
                            .padding(JannahSpacing.xl)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Today's portion is complete.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        Spacer(Modifier.height(8.dp))
                        Text("You have reached the end of today's reading. You can stop here.", style = JannahType.body, color = JannahColors.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Box(
                            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable {
                                KhatmStore.markDayCompleted(ctx)
                                onBack()
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Mark Day Complete", style = JannahType.button, color = JannahColors.OnOrange)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Text("Keep Reading", style = JannahType.bodySmall, color = JannahColors.Ink, modifier = Modifier.pressClickable { showStopOverlay = false }.padding(8.dp))
                    }
                }
            }
        }
    }
}
