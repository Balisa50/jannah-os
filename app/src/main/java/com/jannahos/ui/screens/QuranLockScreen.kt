package com.jannahos.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.pressClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
        Box(Modifier.fillMaxWidth().background(JannahColors.Orange).padding(JannahSpacing.gutter)) {
            Text("Choose a Surah to read", color = JannahColors.OnOrange, style = JannahType.title, fontWeight = FontWeight.Bold)
        }
        androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize()) {
            items(QuranCache.SURAHS.size) { index ->
                val surah = QuranCache.SURAHS[index]
                val id = surah.first
                val name = surah.second
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable { onPicked(id) }
                        .padding(horizontal = JannahSpacing.gutter, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$id", color = JannahColors.OrangeDeep, fontSize = 14.sp, modifier = Modifier.width(32.dp))
                    Text(name, fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
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
        initialPage = (com.jannahos.QuranStore.firstPageOfSurah(surah) - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )
    val currentPage = pagerState.currentPage + 1

    var remaining by remember { mutableLongStateOf(QuranReadingLock.getSavedRemainingMs(ctx)) }
    
    DisposableEffect(Unit) {
        onDispose { QuranReadingLock.saveRemainingMs(ctx, remaining) }
    }

    LaunchedEffect(Unit) {
        while (remaining > 0L) {
            delay(1000)
            remaining -= 1000L
            if (remaining <= 0L) {
                remaining = 0L
                QuranReadingLock.markDoneToday(ctx)
            }
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)
    val surahName = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah"

    Box(Modifier.fillMaxSize().background(Color(0xFFFFFCF3))) {
        // Image Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
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
            }
        }

        // Floating Timer Header
        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(JannahColors.Orange).padding(JannahSpacing.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("READING LOCK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Text(surahName, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
                Text(timeText, fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
        }

        // Floating Bottom Button
        val isDone = remaining == 0L
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(JannahSpacing.m)
                .height(52.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(if (isDone) JannahColors.Ink else JannahColors.Ink.copy(alpha=0.8f))
                .pressClickable(enabled = isDone) {
                    QuranReadingLock.resetSession(ctx)
                    onRelease()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(if (isDone) "Finish reading" else "Keep reading to unlock ($currentPage/604)", style = JannahType.button, color = JannahColors.OnOrange)
        }
    }
}
