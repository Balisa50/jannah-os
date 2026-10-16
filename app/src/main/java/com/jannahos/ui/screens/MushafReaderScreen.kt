package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.jannahos.AyahPlaylist
import com.jannahos.QuranApi
import com.jannahos.QuranBookmarkStore
import com.jannahos.QuranCache
import com.jannahos.QuranReaderPrefs
import com.jannahos.Reciters
import com.jannahos.TafsirApi
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val AmiriQuran = FontFamily(Font(com.jannahos.R.font.amiri_quran))

@Composable
fun MushafReaderScreen(
    surah: Int,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val cache = remember { QuranCache(ctx) }
    val bookmarks = remember { QuranBookmarkStore(ctx) }
    val scope = rememberCoroutineScope()

    var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bookmarkTick by remember { mutableStateOf(0) }
    val ignore = bookmarkTick

    var fontSize by remember { mutableStateOf(QuranReaderPrefs.fontSize(ctx)) }
    var showTranslation by remember { mutableStateOf(QuranReaderPrefs.showTranslation(ctx)) }

    var selectedAyah by remember { mutableStateOf<QuranApi.Ayah?>(null) }
    var tafsirFor by remember { mutableStateOf<QuranApi.Ayah?>(null) }
    var tafsirText by remember { mutableStateOf<String?>(null) }
    var tafsirLoading by remember { mutableStateOf(false) }

    var playingAyah by remember { mutableStateOf(0) }

    val listState = rememberLazyListState()
    val player = remember { AyahPlaylist(ctx) }

    DisposableEffect(Unit) {
        player.setListener(object : AyahPlaylist.Listener {
            override fun onAyahStarted(s: Int, a: Int) {
                playingAyah = a
                // Auto-scroll to center the ayah in view.
                val idx = ayahs.indexOfFirst { it.number == a }
                if (idx >= 0) {
                    scope.launch {
                        // header offset is +1 for ornamental surah header +1 for basmalah if present
                        val headerOffset = if (surah != 1 && surah != 9) 2 else 1
                        val target = (idx + headerOffset).coerceAtLeast(0)
                        listState.animateScrollToItem(target, scrollOffset = -300)
                    }
                }
            }
            override fun onSurahFinished(s: Int) { playingAyah = 0 }
        })
        onDispose { player.stop() }
    }

    // Persist last read ayah as user scrolls.
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
            val headerOffset = if (surah != 1 && surah != 9) 2 else 1
            val ayahIdx = (idx - headerOffset).coerceAtLeast(0)
            ayahs.getOrNull(ayahIdx)?.let { a ->
                QuranReaderPrefs.setLastSurah(ctx, surah)
                QuranReaderPrefs.setLastAyah(ctx, a.number)
            }
        }
    }

    LaunchedEffect(surah) {
        loading = true
        QuranReaderPrefs.setLastSurah(ctx, surah)
        try {
            ayahs = withContext(Dispatchers.IO) {
                if (cache.isCached(surah)) cache.load(surah)
                else {
                    val fetched = QuranApi.fetchSurah(surah)
                    cache.save(surah, fetched)
                    fetched
                }
            }
            error = null
        } catch (e: Exception) {
            error = "No connection. Try again when online."
        }
        loading = false
    }

    LaunchedEffect(tafsirFor) {
        val a = tafsirFor ?: return@LaunchedEffect
        tafsirLoading = true
        tafsirText = withContext(Dispatchers.IO) { TafsirApi.fetch(surah, a.number) }
        tafsirLoading = false
    }

    val surahName = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
    val isTawbah = surah == 9
    val isFatihah = surah == 1
    val reciter = remember { Reciters.current(ctx) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        // Top toolbar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.l, bottom = JannahSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Back", style = JannahType.bodySmall, color = JannahColors.Muted,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.weight(1f))
            ToolbarChip("${fontSize}sp", active = false) {}
            Spacer(Modifier.width(4.dp))
            ToolbarChip("A+", active = false) {
                fontSize = (fontSize + 4).coerceAtMost(48)
                QuranReaderPrefs.setFontSize(ctx, fontSize)
            }
            Spacer(Modifier.width(4.dp))
            ToolbarChip("A−", active = false) {
                fontSize = (fontSize - 4).coerceAtLeast(18)
                QuranReaderPrefs.setFontSize(ctx, fontSize)
            }
            Spacer(Modifier.width(4.dp))
            ToolbarChip("T", active = showTranslation) {
                showTranslation = !showTranslation
                QuranReaderPrefs.setShowTranslation(ctx, showTranslation)
            }
        }

        // Surah title
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(bottom = JannahSpacing.m)
        ) {
            Text(surahName,
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text("Reciter · ${reciter.label}",
                style = JannahType.caption, color = JannahColors.Muted)
        }

        // Mushaf body
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
                    bottom = 140.dp
                )
            ) {
                // Ornamental surah header
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(vertical = JannahSpacing.m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(surahName,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OrangeDeep,
                            textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                }

                // Basmalah — separate header line
                if (!isTawbah && !isFatihah) {
                    item {
                        Text(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = AmiriQuran,
                            fontSize = (fontSize + 2).sp,
                            color = JannahColors.Ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = (fontSize * 1.9f).sp
                        )
                        Spacer(Modifier.height(JannahSpacing.l))
                    }
                }

                // Ayah list — flowing but scrollable
                items(
                    count = ayahs.size,
                    key = { i -> ayahs[i].number }
                ) { i ->
                    val a = ayahs[i]
                    val isPlaying = playingAyah == a.number
                    val isSelected = selectedAyah?.number == a.number

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isPlaying -> JannahColors.OrangeSoft
                                    isSelected -> JannahColors.OrangeSoft
                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                            .clickable { selectedAyah = a }
                            .padding(vertical = 4.dp)
                    ) {
                        // Arabic text with inline ayah marker
                        Text(
                            text = buildString {
                                append(a.arabic.trim())
                                append("   ۝")
                                append(toArabicIndic(a.number))
                            },
                            fontFamily = AmiriQuran,
                            fontSize = fontSize.sp,
                            color = when {
                                isPlaying -> JannahColors.OrangeDeep
                                isSelected -> JannahColors.OrangeDeep
                                else -> JannahColors.Ink
                            },
                            lineHeight = (fontSize * 1.9f).sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (showTranslation) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "(${a.number}) ${a.english}",
                                style = JannahType.bodySmall,
                                color = JannahColors.Body,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // Selected-ayah sheet
        if (selectedAyah != null) {
            val a = selectedAyah!!
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(JannahColors.Surface)
                    .padding(JannahSpacing.m)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${surahName} · ${surah}:${a.number}",
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink,
                            modifier = Modifier.weight(1f))
                        Text("×",
                            style = JannahType.body, color = JannahColors.Muted,
                            modifier = Modifier
                                .clickable { selectedAyah = null }
                                .padding(8.dp))
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                    ) {
                        ActionChip(Modifier.weight(1f), "▶ Play from here") {
                            player.playSurah(surah, a.number, ayahs.lastOrNull()?.number ?: a.number, ayahs)
                            selectedAyah = null
                        }
                        ActionChip(
                            Modifier.weight(1f),
                            if (bookmarks.isBookmarked(surah, a.number)) "★ Saved" else "☆ Save"
                        ) {
                            if (bookmarks.isBookmarked(surah, a.number)) bookmarks.remove(surah, a.number)
                            else bookmarks.add(surah, a.number)
                            bookmarkTick++
                            selectedAyah = null
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                    ) {
                        ActionChip(Modifier.weight(1f), "Tafsir") {
                            tafsirFor = a
                            selectedAyah = null
                        }
                        ActionChip(
                            Modifier.weight(1f),
                            if (playingAyah != 0) "■ Stop" else "Play surah"
                        ) {
                            if (playingAyah != 0) {
                                player.stop()
                                playingAyah = 0
                            } else {
                                player.playSurah(surah, ayahs.first().number, ayahs.last().number, ayahs)
                            }
                            selectedAyah = null
                        }
                    }
                }
            }
        }

        // Tafsir panel
        if (tafsirFor != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(JannahColors.Surface)
                    .padding(JannahSpacing.m)
            ) {
                Column(
                    Modifier
                        .height(220.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tafsir · ${surah}:${tafsirFor!!.number}",
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink,
                            modifier = Modifier.weight(1f))
                        Text("×",
                            style = JannahType.body, color = JannahColors.Muted,
                            modifier = Modifier
                                .clickable { tafsirFor = null; tafsirText = null }
                                .padding(8.dp))
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                    if (tafsirLoading) {
                        Text("Loading tafsir...",
                            style = JannahType.caption, color = JannahColors.Muted)
                    } else if (tafsirText != null) {
                        Text(tafsirText!!,
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 22.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("Ibn Kathir (abridged) · via quran.com",
                            style = JannahType.micro, color = JannahColors.Faint)
                    } else {
                        Text("Tafsir unavailable offline.",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(if (active) JannahColors.Orange else JannahColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, style = JannahType.micro,
            color = if (active) JannahColors.OnOrange else JannahColors.Ink)
    }
}

@Composable
private fun ActionChip(modifier: Modifier = Modifier, label: String, onClick: () -> Unit) {
    Box(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.OrangeSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = JannahType.caption,
            color = JannahColors.OrangeDeep,
            fontWeight = FontWeight.SemiBold)
    }
}


private fun toArabicIndic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
