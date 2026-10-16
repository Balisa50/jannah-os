package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.AyahPlayer
import com.jannahos.KhatmPlan
import com.jannahos.QuranApi
import com.jannahos.QuranBookmarkStore
import com.jannahos.QuranBookmarks
import com.jannahos.QuranCache
import com.jannahos.Reciters
import com.jannahos.TafsirApi
import com.jannahos.WordByWordApi
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuranTabScreen() {
    var selected by remember { mutableStateOf<Int?>(null) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showReciters by remember { mutableStateOf(false) }
    var showKhatm by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    if (showKhatm) { KhatmScreen(onBack = { showKhatm = false }); return }
    if (showSearch) { QuranSearchScreen(onBack = { showSearch = false }); return }
    if (showReciters) { ReciterPickerScreen(onBack = { showReciters = false }); return }
    if (showBookmarks) {
        BookmarksScreen(
            onBack = { showBookmarks = false },
            onOpen = { s, _ -> selected = s; showBookmarks = false }
        )
        return
    }

    if (selected == null) SurahList(
        onPick = { selected = it },
        onOpenBookmarks = { showBookmarks = true },
        onOpenReciters = { showReciters = true },
        onOpenKhatm = { showKhatm = true },
        onOpenSearch = { showSearch = true }
    )
    else MushafReaderScreen(surah = selected!!, onBack = { selected = null })
}

@Composable
private fun SurahList(
    onPick: (Int) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenReciters: () -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val ctx = LocalContext.current
    val last = remember { QuranBookmarks.lastSurah(ctx) }
    val bookmarkCount = remember { QuranBookmarkStore(ctx).all().size }
    val reciter = remember { Reciters.current(ctx) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("QURAN",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("114 ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("surahs.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Reciter · ${reciter.label}",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable(onClick = onOpenReciters)
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Reciter",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(reciter.label, style = JannahType.caption,
                            color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.s))

            if (last != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .clickable { onPick(last) }
                        .padding(JannahSpacing.l)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("CONTINUE", style = JannahType.eyebrow,
                                color = JannahColors.OrangeDeep)
                            Spacer(Modifier.height(JannahSpacing.xs))
                            val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second
                                ?: "Surah $last"
                            Text(name,
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                        }
                        Text("→", style = JannahType.title, color = JannahColors.Orange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.s))
            }

            // Search entry
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable(onClick = onOpenSearch)
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Search the Quran",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text("Find any verse by word",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }
            Spacer(Modifier.height(JannahSpacing.s))

            // Reading plan entry
            val planDays = KhatmPlan.planDays(ctx)
            val planDone = KhatmPlan.completedDays(ctx).size
            val planText = if (planDays == 0) "Start a plan"
                else "$planDone of $planDays days"
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable { onOpenKhatm() }
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Reading plan",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(planText, style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }
            Spacer(Modifier.height(JannahSpacing.s))

            if (bookmarkCount > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .clickable(onClick = onOpenBookmarks)
                        .padding(JannahSpacing.l)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Bookmarks",
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text("$bookmarkCount saved", style = JannahType.caption,
                                color = JannahColors.Muted)
                        }
                        Text("→", style = JannahType.title, color = JannahColors.Orange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.s))
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Text("SURAHS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            QuranCache.SURAHS.forEachIndexed { i, (num, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(num) }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$num", style = JannahType.caption,
                        color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < QuranCache.SURAHS.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahReader(surah: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val cache = remember { QuranCache(ctx) }
    val bookmarks = remember { QuranBookmarkStore(ctx) }
    val reciter = remember { Reciters.current(ctx) }
    val player = remember { AyahPlayer() }
    var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tafsirAyah by remember { mutableStateOf<Int?>(null) }
    var tafsirText by remember { mutableStateOf<String?>(null) }
    var tafsirLoading by remember { mutableStateOf(false) }
    var bookmarkTick by remember { mutableStateOf(0) }
    var playingGlobal by remember { mutableStateOf<Int?>(null) }
    var wordMode by remember { mutableStateOf(false) }
    var wordsData by remember { mutableStateOf<List<List<WordByWordApi.Word>>>(emptyList()) }
    var wordsLoading by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<WordByWordApi.Word?>(null) }

    DisposableEffect(Unit) {
        onDispose { player.stop() }
    }

    LaunchedEffect(surah) {
        loading = true
        QuranBookmarks.setLastSurah(ctx, surah)
        try {
            val list = withContext(Dispatchers.IO) {
                if (cache.isCached(surah)) cache.load(surah)
                else {
                    val fetched = QuranApi.fetchSurah(surah)
                    cache.save(surah, fetched)
                    fetched
                }
            }
            ayahs = list
            error = null
        } catch (e: Exception) {
            error = "No connection. Try again when online."
        }
        loading = false
    }

    LaunchedEffect(tafsirAyah) {
        val a = tafsirAyah ?: return@LaunchedEffect
        tafsirLoading = true
        tafsirText = withContext(Dispatchers.IO) { TafsirApi.fetch(surah, a) }
        tafsirLoading = false
    }

    LaunchedEffect(wordMode) {
        if (wordMode && wordsData.isEmpty()) {
            wordsLoading = true
            wordsData = withContext(Dispatchers.IO) {
                try { WordByWordApi.fetch(surah) } catch (e: Exception) { emptyList() }
            }
            wordsLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("← Back", style = JannahType.bodySmall, color = JannahColors.Muted,
            modifier = Modifier.clickable(onClick = onBack))

        Spacer(Modifier.height(JannahSpacing.m))

        val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
        Row(verticalAlignment = Alignment.Bottom) {
            Text(name, style = JannahType.title, color = JannahColors.Ink)
            Spacer(Modifier.width(8.dp))
            Text("reciter · ${reciter.label}",
                style = JannahType.micro, color = JannahColors.Faint,
                modifier = Modifier.padding(bottom = 4.dp))
        }

        Spacer(Modifier.height(JannahSpacing.s))

        // Word-by-word toggle
        Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(if (wordMode) JannahColors.Orange else JannahColors.Card)
                    .clickable { wordMode = !wordMode }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    if (wordMode) "Word mode · on" else "Word mode · off",
                    style = JannahType.caption,
                    color = if (wordMode) JannahColors.OnOrange else JannahColors.Muted
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        when {
            loading -> Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
            error != null -> Text(error!!, style = JannahType.bodySmall,
                color = JannahColors.Muted, textAlign = TextAlign.Center)
            else -> ayahs.forEach { a ->
                val isBookmarked = bookmarks.isBookmarked(surah, a.number)
                val ignore = bookmarkTick
                val isPlaying = playingGlobal == a.globalNumber

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = JannahSpacing.m),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val words = if (wordMode && wordsData.size >= a.number)
                        wordsData[a.number - 1] else emptyList()

                    if (wordMode && words.isNotEmpty()) {
                        // Render each word as a tappable chip.
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            words.forEach { w ->
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(JannahColors.Card)
                                        .clickable { selectedWord = w }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        w.arabic,
                                        fontSize = 26.sp,
                                        color = JannahColors.Ink
                                    )
                                }
                            }
                        }
                    } else {
                        Text(a.arabic, style = JannahType.arabic, color = JannahColors.Ink,
                            textAlign = TextAlign.Center)
                    }

                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("(${a.number}) ${a.english}",
                        style = JannahType.bodySmall, color = JannahColors.Body,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)

                    if (wordMode && wordsLoading) {
                        Spacer(Modifier.height(JannahSpacing.xs))
                        Text("Loading word data...", style = JannahType.micro,
                            color = JannahColors.Faint)
                    }
                    Spacer(Modifier.height(JannahSpacing.s))

                    Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.m)) {
                        Text(
                            if (isPlaying) "■ stop" else "▶ play",
                            style = JannahType.caption,
                            color = JannahColors.Orange,
                            modifier = Modifier.clickable {
                                if (isPlaying) {
                                    player.stop()
                                    playingGlobal = null
                                } else if (a.globalNumber > 0) {
                                    playingGlobal = a.globalNumber
                                    player.play(
                                        Reciters.audioUrl(reciter, a.globalNumber)
                                    ) { playingGlobal = null }
                                }
                            }
                        )
                        Text(
                            if (isBookmarked) "★ saved" else "☆ bookmark",
                            style = JannahType.caption,
                            color = if (isBookmarked) JannahColors.Orange else JannahColors.Muted,
                            modifier = Modifier.clickable {
                                if (isBookmarked) bookmarks.remove(surah, a.number)
                                else bookmarks.add(surah, a.number)
                                bookmarkTick++
                            }
                        )
                        Text(
                            if (tafsirAyah == a.number) "hide tafsir" else "tafsir",
                            style = JannahType.caption, color = JannahColors.Orange,
                            modifier = Modifier.clickable {
                                tafsirAyah = if (tafsirAyah == a.number) null else a.number
                            }
                        )
                    }

                    if (tafsirAyah == a.number) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                                .background(JannahColors.Card)
                                .padding(JannahSpacing.m)
                        ) {
                            Column {
                                if (tafsirLoading) {
                                    Text("Loading tafsir...", style = JannahType.caption,
                                        color = JannahColors.Muted)
                                } else if (tafsirText != null) {
                                    Text(tafsirText!!, style = JannahType.bodySmall,
                                        color = JannahColors.Body, textAlign = TextAlign.Left,
                                        lineHeight = 22.sp)
                                    Spacer(Modifier.height(JannahSpacing.s))
                                    Text("Ibn Kathir (abridged) · via quran.com",
                                        style = JannahType.micro, color = JannahColors.Faint)
                                } else {
                                    Text("Tafsir unavailable (offline).",
                                        style = JannahType.caption, color = JannahColors.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))

        // Word meaning card shown when a word is tapped
        if (selectedWord != null) {
            val w = selectedWord!!
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(w.arabic, fontSize = 34.sp, color = JannahColors.Ink)
                        Spacer(Modifier.width(JannahSpacing.m))
                        Column(Modifier.weight(1f)) {
                            Text(w.transliteration, style = JannahType.bodySmall,
                                color = JannahColors.OrangeDeep)
                            if (!w.root.isNullOrBlank()) {
                                Text("root · ${w.root}", style = JannahType.micro,
                                    color = JannahColors.Muted)
                            }
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .clickable { selectedWord = null }
                                .padding(8.dp)
                        ) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                    }
                    if (w.meaning.isNotBlank()) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(w.meaning, style = JannahType.body, color = JannahColors.Body,
                            lineHeight = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(JannahSpacing.m))
        }
    }
}

@Composable
private fun ReciterPickerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var current by remember { mutableStateOf(Reciters.current(ctx)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("RECITER", style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Choose a ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("voice.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Reciters.all.forEachIndexed { i, r ->
                val selected = r.id == current.id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            Reciters.set(ctx, r)
                            current = r
                        }
                        .padding(vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(r.label,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    if (selected) Text("✓", style = JannahType.title, color = JannahColors.Orange)
                }
                if (i < Reciters.all.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun BookmarksScreen(onBack: () -> Unit, onOpen: (Int, Int) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { QuranBookmarkStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val marks = store.all()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("SAVED", style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Your ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("bookmarks.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (marks.isEmpty()) {
                Text("No bookmarks yet. Tap ☆ on any verse to save it here.",
                    style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp)
            } else {
                marks.forEachIndexed { i, m ->
                    val surahName = QuranCache.SURAHS.firstOrNull { it.first == m.surah }?.second
                        ?: "Surah ${m.surah}"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(m.surah, m.ayah) }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("$surahName · ${m.surah}:${m.ayah}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            if (!m.note.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(m.note, style = JannahType.caption,
                                    color = JannahColors.Muted)
                            }
                        }
                        Text(
                            "×",
                            style = JannahType.body,
                            color = JannahColors.Muted,
                            modifier = Modifier
                                .clickable {
                                    store.remove(m.surah, m.ayah)
                                    refresh++
                                }
                                .padding(8.dp)
                        )
                    }
                    if (i < marks.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
