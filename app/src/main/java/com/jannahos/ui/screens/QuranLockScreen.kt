package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.jannahos.QuranCache
import com.jannahos.QuranReadingLock
import com.jannahos.QuranStore
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun QuranLockScreen(onRelease: () -> Unit) {
    var selectedSurah by remember { mutableStateOf<Int?>(null) }

    if (selectedSurah == null) {
        SurahPickerScreen(onPicked = { selectedSurah = it })
    } else {
        ReadingLockSession(surah = selectedSurah!!, onRelease = onRelease)
    }
}

@Composable
private fun SurahPickerScreen(onPicked: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().background(JannahColors.Orange).padding(JannahSpacing.gutter).windowInsetsPadding(WindowInsets.statusBars)) {
            Text("Choose a Surah to read", color = JannahColors.OnOrange, style = JannahType.title, fontWeight = FontWeight.Bold)
        }
        androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize()) {
            items(QuranCache.SURAHS.size) { index ->
                val surah = QuranCache.SURAHS[index]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable { onPicked(surah.first) }
                        .padding(horizontal = JannahSpacing.gutter, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${surah.first}", color = JannahColors.OrangeDeep, fontSize = 14.sp, modifier = Modifier.width(32.dp))
                    Text(surah.second, fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
        }
    }
}

@Composable
private fun ReadingLockSession(surah: Int, onRelease: () -> Unit) {
    val ctx = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = (QuranStore.firstPageOfSurah(surah) - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )

    var remaining by remember { mutableLongStateOf(QuranReadingLock.getSavedRemainingMs(ctx)) }
    var isDone by remember { mutableStateOf(remaining <= 0L) }
    
    // Pause timer instantly when leaving screen
    DisposableEffect(Unit) {
        onDispose { QuranReadingLock.saveRemainingMs(ctx, remaining) }
    }

    LaunchedEffect(Unit) {
        if (isDone) {
            QuranReadingLock.markDoneToday(ctx)
        } else {
            while (remaining > 0L) {
                delay(1000)
                remaining -= 1000L
            }
            QuranReadingLock.markDoneToday(ctx)
            isDone = true
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)

    Box(Modifier.fillMaxSize().background(Color(0xFFFFFCF3))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), pageSpacing = 0.dp) { pageIndex ->
            val pageNum = pageIndex + 1
            val bitmap = remember(pageNum) { MushafImageStore.loadBitmap(ctx, pageNum) }
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Page $pageNum",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(JannahColors.Orange).padding(horizontal = JannahSpacing.gutter, vertical = 12.dp).windowInsetsPadding(WindowInsets.statusBars)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isDone) "UNLOCKED" else "READING LOCK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
                Text(timeText, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
        }

        if (isDone) {
            // Overlay to celebrate completion and allow exit
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(JannahSpacing.gutter),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Orange)
                        .pressClickable { onRelease() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Exit to Jannah", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
