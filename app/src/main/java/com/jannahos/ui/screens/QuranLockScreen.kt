package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranApi
import com.jannahos.QuranCache
import com.jannahos.QuranReaderPrefs
import com.jannahos.QuranReadingLock
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))

@Composable
fun QuranLockScreen(onRelease: () -> Unit) {
    val ctx = LocalContext.current
    val surah = remember { if (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY) 18 else 67 }
    val surahName = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah"
    val cache = remember { QuranCache(ctx) }

    var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(surah) {
        loading = true
        try {
            ayahs = withContext(Dispatchers.IO) {
                if (cache.isCached(surah)) cache.load(surah)
                else {
                    val f = QuranApi.fetchSurah(surah)
                    cache.save(surah, f)
                    f
                }
            }
            error = null
        } catch (e: Exception) {
            error = "Connect to the internet."
        }
        loading = false
    }

    // Timer Pause Logic
    var remaining by remember { mutableStateOf(QuranReadingLock.getSavedRemainingMs(ctx)) }
    DisposableEffect(Unit) {
        onDispose { QuranReadingLock.saveRemainingMs(ctx, remaining) }
    }

    LaunchedEffect(Unit) {
        while (remaining > 0L) {
            delay(1000)
            remaining -= 1000L
            if (remaining <= 0L) {
                remaining = 0L
            }
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)

    val listState = rememberLazyListState()
    var lastIndexSeen by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
            if (idx > lastIndexSeen) {
                QuranReadingLock.incrementLines(ctx, idx - lastIndexSeen)
                lastIndexSeen = idx
            }
        }
    }

    val linesRead = QuranReadingLock.linesRead(ctx)
    val progress by animateFloatAsState((linesRead.toFloat() / QuranReadingLock.LINES_PER_PAGE).coerceIn(0f, 1f), tween(400), label = "p")

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().background(JannahColors.Orange).padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("READING LOCK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(2.dp))
                        Text(surahName, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                    }
                    Text(timeText, fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.s))
                Text("Read $linesRead of ${QuranReadingLock.LINES_PER_PAGE} lines. Leaving pauses the timer.", style = JannahType.caption, color = JannahColors.OnOrange.copy(alpha = 0.9f))
                Spacer(Modifier.height(JannahSpacing.s))
                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.OnOrange.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(3.dp)).background(JannahColors.OnOrange))
                }
            }
        }

        if (loading) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted) }
        } else if (error != null) {
            Box(Modifier.weight(1f).padding(JannahSpacing.gutter), contentAlignment = Alignment.Center) { Text(error!!, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center) }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = JannahSpacing.gutter), contentPadding = PaddingValues(top = JannahSpacing.l, bottom = JannahSpacing.xxl)) {
                items(count = ayahs.size, key = { i -> ayahs[i].number }) { i ->
                    val a = ayahs[i]
                    Text(
                        text = "${a.arabic} ۝",
                        fontFamily = UthmanicHafs,
                        fontSize = QuranReaderPrefs.fontSize(ctx).sp,
                        color = JannahColors.Ink,
                        lineHeight = (QuranReaderPrefs.fontSize(ctx) * 2f).sp,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }
        }

        val satisfied = QuranReadingLock.isSatisfied(ctx) && remaining == 0L
        Box(
            Modifier.fillMaxWidth().padding(JannahSpacing.m).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(if (satisfied) JannahColors.Ink else JannahColors.Border)
                .clickable(enabled = satisfied) {
                    QuranReadingLock.markDoneToday(ctx)
                    QuranReadingLock.resetSession(ctx)
                    onRelease()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(if (satisfied) "Finish reading" else "Keep reading to unlock", style = JannahType.button, color = if (satisfied) JannahColors.OnOrange else JannahColors.Faint)
        }
    }
}
