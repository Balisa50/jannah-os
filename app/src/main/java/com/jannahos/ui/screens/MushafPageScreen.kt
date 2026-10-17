package com.jannahos.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranReaderPrefs
import com.jannahos.QuranStore
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.pressClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))
private const val BASMALLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

// Madani mushaf: page where each surah starts (index = surah number)
private val SURAH_START = intArrayOf(0,
    1,2,50,77,106,128,151,177,187,208,221,235,249,255,262,267,282,293,305,312,
    322,332,342,350,359,367,377,385,396,404,411,415,418,428,434,440,446,453,458,467,
    477,483,489,496,499,502,507,511,515,518,520,523,526,528,531,534,537,542,545,549,
    551,553,554,556,558,560,562,564,566,568,570,572,574,575,577,578,580,582,583,585,
    586,587,587,589,590,591,591,592,593,594,595,595,596,596,597,597,598,598,599,599,
    600,600,601,601,601,602,602,602,603,603,603,604,604,604)

private object PageCache {
    private val map = ConcurrentHashMap<Int, QuranStore.PageLayout>()
    fun get(p: Int): QuranStore.PageLayout? = map[p]
    suspend fun load(ctx: Context, p: Int): QuranStore.PageLayout? {
        map[p]?.let { return it }
        val r = withContext(Dispatchers.IO) { QuranStore.loadPage(ctx, p) }
        if (r != null) map[p] = r
        return r
    }
}

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
        initialPage = startPage.coerceIn(1, QuranStore.totalPages()) - 1,
        pageCount = { QuranStore.totalPages() }
    )
    val fontSize = remember { QuranReaderPrefs.fontSize(ctx) }
    val currentPage = pagerState.currentPage + 1
    var currentSurah by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        PageCache.load(ctx, currentPage)?.let { currentSurah = it.firstSurah }
        // prefetch neighbours so swipes are instant
        for (p in (currentPage - 1)..(currentPage + 3)) {
            if (p in 1..QuranStore.totalPages()) PageCache.load(ctx, p)
        }
    }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == currentSurah }?.second ?: "Quran",
                onBack = onBack
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { idx -> MushafPageItem(ctx, idx + 1, fontSize) }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavPill("←") { if (pagerState.currentPage > 0) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                Text("$currentPage / 604", style = JannahType.caption, color = JannahColors.Muted)
                NavPill("→") { if (pagerState.currentPage < QuranStore.totalPages() - 1) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            }
        }

        AudioFab(
            surah = currentSurah,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 80.dp)
        )
    }
}

@Composable
private fun AudioFab(surah: Int, modifier: Modifier) {
    val ctx = LocalContext.current
    var playing by remember { mutableStateOf(false) }
    var preparing by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(surah) {
        player?.release(); player = null; playing = false; preparing = false
    }
    DisposableEffect(Unit) { onDispose { player?.release() } }

    FloatingActionButton(
        onClick = {
            val mp = player
            if (playing) {
                mp?.pause(); playing = false
            } else if (mp != null && !preparing) {
                mp.start(); playing = true
            } else if (!preparing) {
                preparing = true
                try {
                    val url = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surah.mp3"
                    player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA).build()
                        )
                        setDataSource(url)
                        setOnPreparedListener { it.start(); playing = true; preparing = false }
                        setOnCompletionListener { playing = false }
                        setOnErrorListener { _, _, _ ->
                            preparing = false; playing = false
                            Toast.makeText(ctx, "Audio failed. Check your internet.", Toast.LENGTH_SHORT).show()
                            true
                        }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    preparing = false
                    Toast.makeText(ctx, "Audio failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        containerColor = JannahColors.Orange,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = modifier.size(56.dp)
    ) {
        if (preparing) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        else Text(if (playing) "❚❚" else "▶", fontSize = 18.sp)
    }
}

@Composable
private fun FitLine(text: String, baseSize: Int) {
    var size by remember(text, baseSize) { mutableFloatStateOf(baseSize.toFloat()) }
    Text(
        text = text,
        fontFamily = UthmanicHafs,
        fontSize = size.sp,
        lineHeight = (size * 1.8f).sp,
        color = JannahColors.Ink,
        textAlign = TextAlign.Center,
        softWrap = false,
        maxLines = 1,
        onTextLayout = { if (it.didOverflowWidth && size > 12f) size *= 0.93f },
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}

@Composable
private fun MushafPageItem(ctx: Context, pageNum: Int, fontSize: Int) {
    var layout by remember(pageNum) { mutableStateOf(PageCache.get(pageNum)) }
    var done by remember(pageNum) { mutableStateOf(layout != null) }

    LaunchedEffect(pageNum) {
        if (layout == null) { layout = PageCache.load(ctx, pageNum); done = true }
    }

    val l = layout
    if (l == null) {
        // no empty card flash: just a spinner on the cream background
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!done) CircularProgressIndicator(color = JannahColors.Orange)
            else Text("Page not downloaded.", style = JannahType.bodySmall, color = JannahColors.Muted)
        }
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFCF3))
            .border(1.dp, Color(0xFFE0D5B8), RoundedCornerShape(12.dp))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.m)
        ) {
            Text("الجزء ${juzForPage(pageNum)}", fontFamily = UthmanicHafs, fontSize = 12.sp, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0D5B8)))
            Spacer(Modifier.height(JannahSpacing.m))

            // Surah banner + Basmalah when a surah starts at the top of this page
            val s = l.firstSurah
            val startsHere = s in 1..114 && SURAH_START[s] == pageNum
            if (startsHere && s != 1 && s != 9 && l.basmalaLineNumber == null) {
                val name = com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == s }?.second ?: ""
                Text(name, style = JannahType.caption, color = JannahColors.OrangeDeep,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                FitLine(BASMALLAH, fontSize)
                Spacer(Modifier.height(8.dp))
            }

            l.lines.toSortedMap().forEach { (_, text) -> FitLine(text, fontSize) }

            Spacer(Modifier.height(JannahSpacing.l))
            Text("$pageNum", fontFamily = UthmanicHafs, fontSize = 14.sp, color = JannahColors.OrangeDeep,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun NavPill(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Card)
            .pressClickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) { Text(label, style = JannahType.title, color = JannahColors.Ink) }
}

private fun juzForPage(page: Int): Int {
    val starts = intArrayOf(1,22,42,62,82,102,121,142,162,181,201,222,242,262,282,302,322,342,362,382,402,422,442,462,482,502,522,542,562,582)
    for (i in starts.indices.reversed()) if (page >= starts[i]) return i + 1
    return 1
}
