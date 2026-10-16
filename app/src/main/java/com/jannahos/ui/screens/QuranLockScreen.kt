package com.jannahos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val AmiriQuran = FontFamily(Font(com.jannahos.R.font.amiri_quran))

// Suggests a surah for today's reading. Picks based on what was last read
// or defaults to Al-Kahf on Friday, Al-Mulk otherwise.
private fun suggestedSurah(): Int {
    val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    return if (day == java.util.Calendar.FRIDAY) 18 else 67
}

@Composable
fun QuranLockScreen(onRelease: () -> Unit) {
    val ctx = LocalContext.current

    // Start / resume today's session
    LaunchedEffect(Unit) {
        QuranReadingLock.startOrResume(ctx)
    }

    // Reset the timer if user navigates away from this screen.
    DisposableEffect(Unit) {
        onDispose {
            if (!QuranReadingLock.isSatisfied(ctx)) {
                QuranReadingLock.resetTimer(ctx)
            }
        }
    }

    val surah = remember { suggestedSurah() }
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
            error = "Connect to the internet to load this surah for the first time."
        }
        loading = false
    }

    // Countdown timer — every second, refresh remaining.
    var remaining by remember { mutableStateOf(QuranReadingLock.remainingMs(ctx)) }
    LaunchedEffect(Unit) {
        while (true) {
            remaining = QuranReadingLock.remainingMs(ctx)
            if (QuranReadingLock.isSatisfied(ctx)) {
                QuranReadingLock.markDoneToday(ctx)
                QuranReadingLock.endSession(ctx)
                onRelease()
                return@LaunchedEffect
            }
            delay(500)
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)

    // Track lines read by observing scroll index.
    val listState = rememberLazyListState()
    var lastIndexSeen by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
            if (idx > lastIndexSeen) {
                val delta = idx - lastIndexSeen
                QuranReadingLock.incrementLines(ctx, delta)
                lastIndexSeen = idx
            }
        }
    }

    val linesRead = QuranReadingLock.linesRead(ctx)
    val progress by animateFloatAsState(
        (linesRead.toFloat() / QuranReadingLock.LINES_PER_PAGE).coerceIn(0f, 1f),
        tween(400),
        label = "p"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        // Sticky top: the lock screen header with timer.
        Box(
            Modifier
                .fillMaxWidth()
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("READING LOCK",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(2.dp))
                        Text(surahName,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange)
                    }
                    Text(timeText,
                        fontFamily = FontFamily.Serif,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    "Read $linesRead of ${QuranReadingLock.LINES_PER_PAGE} lines. Leaving resets the timer.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(JannahSpacing.s))
                // Progress bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(JannahColors.OnOrange.copy(alpha = 0.25f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(JannahColors.OnOrange)
                    )
                }
            }
        }

        // Body
        if (loading) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
            }
        } else if (error != null) {
            Box(Modifier.weight(1f).padding(JannahSpacing.gutter), contentAlignment = Alignment.Center) {
                Text(error!!, style = JannahType.bodySmall,
                    color = JannahColors.Muted, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = JannahSpacing.gutter),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = JannahSpacing.l,
                    bottom = JannahSpacing.xxl
                )
            ) {
                items(
                    count = ayahs.size,
                    key = { i -> ayahs[i].number }
                ) { i ->
                    val a = ayahs[i]
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = buildString {
                                append(a.arabic.trim())
                                append("   ۝")
                                append(toArabic(a.number))
                            },
                            fontFamily = AmiriQuran,
                            fontSize = QuranReaderPrefs.fontSize(ctx).sp,
                            color = JannahColors.Ink,
                            lineHeight = (QuranReaderPrefs.fontSize(ctx) * 1.9f).sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom — the "done" affordance, only enabled when the goal is met.
        val satisfied = QuranReadingLock.isSatisfied(ctx)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(JannahSpacing.m)
                .height(52.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(
                    if (satisfied) JannahColors.Ink else JannahColors.Border
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (satisfied) "Finish reading" else "Keep reading to unlock",
                style = JannahType.button,
                color = if (satisfied) JannahColors.OnOrange else JannahColors.Faint
            )
        }
    }
}

private fun toArabic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
