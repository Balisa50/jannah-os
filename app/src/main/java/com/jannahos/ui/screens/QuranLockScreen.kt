package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jannahos.LockState
import com.jannahos.MushafImageStore
import com.jannahos.QuranCache
import com.jannahos.QuranReadingLock
import com.jannahos.QuranStore
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun QuranLockScreen(onRelease: () -> Unit) {
    BackHandler { /* Force them to read */ }
    val ctx = LocalContext.current
    
    LaunchedEffect(Unit) {
        LockState.isLockActive = true
    }

    val savedRemaining = QuranReadingLock.getSavedRemainingMs(ctx)
    val savedSurah = QuranReadingLock.getLastSelectedSurah(ctx)
    
    var selectedSurah by remember { mutableStateOf<Int?>(if (savedRemaining < QuranReadingLock.DEFAULT_MS) savedSurah else null) }

    if (selectedSurah == null) {
        SurahPickerScreen(onPicked = { 
            selectedSurah = it
            QuranReadingLock.setLastSelectedSurah(ctx, it)
        })
    } else {
        ReadingLockSession(
            surah = selectedSurah!!, 
            onRelease = { 
                // 1. Clear the lock state completely
                LockState.isLockActive = false
                LockState.remainingMs = 0L
                
                // 2. Mark it as done for today so the service doesn't re-trigger it
                QuranReadingLock.markDoneToday(ctx)
                
                // 3. Tell MainActivity to hide the lock screen
                onRelease() 
            }
        )
    }
}

@Composable
private fun SurahPickerScreen(onPicked: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().background(JannahColors.Orange).padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.xxl).windowInsetsPadding(WindowInsets.statusBars)) {
            Text("Choose a Surah to read", color = JannahColors.OnOrange, style = JannahType.title, fontWeight = FontWeight.Bold)
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(QuranCache.SURAHS.size) { index ->
                val surah = QuranCache.SURAHS[index]
                Row(
                    Modifier.fillMaxWidth().pressClickable { onPicked(surah.first) }.padding(horizontal = JannahSpacing.gutter, vertical = 16.dp),
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
    var isForeground by remember { mutableStateOf(true) }

    LaunchedEffect(isForeground) {
        if (!isForeground || isDone) return@LaunchedEffect
        while (remaining > 0L && isForeground) {
            delay(1000)
            remaining -= 1000L
            LockState.remainingMs = remaining
        }
        if (remaining <= 0L) {
            isDone = true
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)

    val now = Calendar.getInstance()
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }
    val timeUntilMidnight = midnight.timeInMillis - now.timeInMillis
    val canSnooze = timeUntilMidnight > 15 * 60 * 1000L

    Box(Modifier.fillMaxSize().background(Color(0xFFFFFCF3))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), pageSpacing = 0.dp) { pageIndex ->
            val pageNum = pageIndex + 1
            val bitmap = remember(pageNum) { MushafImageStore.loadBitmap(ctx, pageNum) }
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Page $pageNum",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(JannahColors.Orange).padding(horizontal = JannahSpacing.gutter, vertical = 12.dp).windowInsetsPadding(WindowInsets.statusBars)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isDone) "UNLOCKED" else "READING LOCK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
                
                if (!isDone && canSnooze) {
                    Box(
                        Modifier.clip(RoundedCornerShape(50)).background(JannahColors.OnOrange.copy(alpha = 0.15f)).pressClickable {
                            com.jannahos.QuranLockScheduler.scheduleSnooze(ctx, 15)
                            com.jannahos.QuranReadingLock.setActiveToday(ctx, false)
                            LockState.isLockActive = false
                            onRelease()
                        }.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Snooze 15m", style = JannahType.micro, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                
                Text(timeText, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
        }

        if (isDone) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).windowInsetsPadding(WindowInsets.navigationBars).padding(JannahSpacing.gutter),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { 
                        onRelease() 
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Exit to Jannah", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
