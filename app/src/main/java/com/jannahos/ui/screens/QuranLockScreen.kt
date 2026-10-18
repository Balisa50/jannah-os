package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.jannahos.QuranReadingLock
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))

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
        LazyColumn(Modifier.fillMaxSize()) {
            items(QuranCache.SURAHS) { surah ->
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
    val cache = remember { QuranCache(ctx) }
    
    // Instant load if cached
    var ayahs by remember(surah) { mutableStateOf(if (cache.isCached(surah)) cache.load(surah) else emptyList()) }
    
    LaunchedEffect(surah) {
        if (ayahs.isEmpty()) {
            ayahs = withContext(Dispatchers.IO) {
                try { QuranApi.fetchSurah(surah).also { cache.save(surah, it) } } catch (e: Exception) { emptyList() }
            }
        }
    }

    // Pausing Timer
    var remaining by remember { mutableLongStateOf(QuranReadingLock.getSavedRemainingMs(ctx)) }
    
    // Save state when app is closed/minimized
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

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Box(Modifier.fillMaxWidth().background(JannahColors.Orange).padding(JannahSpacing.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("READING LOCK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Text(surahName, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
                Text(timeText, fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
            }
        }

        if (ayahs.isNotEmpty()) {
            LazyColumn(Modifier.weight(1f).padding(horizontal = JannahSpacing.gutter), contentPadding = PaddingValues(vertical = JannahSpacing.m)) {
                items(ayahs.size) { i ->
                    val a = ayahs[i]
                    Text(
                        text = "${a.arabic} ۝",
                        fontFamily = UthmanicHafs,
                        fontSize = 24.sp,
                        color = JannahColors.Ink,
                        lineHeight = 48.sp,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }
        } else {
            // Skeleton text block, prevents layout jump
            Column(Modifier.weight(1f).padding(JannahSpacing.gutter)) {
                repeat(5) {
                    Box(Modifier.fillMaxWidth().height(24.dp).padding(vertical = 4.dp).background(JannahColors.Border.copy(alpha = 0.5f)))
                }
            }
        }

        val isDone = remaining == 0L
        Box(
            Modifier.fillMaxWidth().padding(JannahSpacing.m).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(if (isDone) JannahColors.Ink else JannahColors.Border)
                .clickable(enabled = isDone) {
                    QuranReadingLock.resetSession(ctx)
                    onRelease()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(if (isDone) "Finish reading" else "Keep reading to unlock", style = JannahType.button, color = if (isDone) JannahColors.OnOrange else JannahColors.Faint)
        }
    }
}
