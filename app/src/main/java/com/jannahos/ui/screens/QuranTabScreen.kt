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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jannahos.QuranApi
import com.jannahos.QuranBookmarks
import com.jannahos.QuranCache
import com.jannahos.TafsirApi
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuranTabScreen() {
    var selected by remember { mutableStateOf<Int?>(null) }
    if (selected == null) SurahList(onPick = { selected = it })
    else SurahReader(surah = selected!!, onBack = { selected = null })
}

@Composable
private fun SurahList(onPick: (Int) -> Unit) {
    val ctx = LocalContext.current
    val last = remember { QuranBookmarks.lastSurah(ctx) }

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
                    Text("Read. ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("Reflect.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (last != null) {
                Text("CONTINUE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
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
                            val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                            Text(name,
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text("Surah $last", style = JannahType.caption, color = JannahColors.Muted)
                        }
                        Text("→", style = JannahType.title, color = JannahColors.Orange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            Text("SURAHS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            QuranCache.SURAHS.forEachIndexed { i, (num, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(num) }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$num", style = JannahType.caption,
                        color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < QuranCache.SURAHS.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}

@Composable
private fun SurahReader(surah: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val cache = remember { QuranCache(ctx) }
    var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tafsirAyah by remember { mutableStateOf<Int?>(null) }
    var tafsirText by remember { mutableStateOf<String?>(null) }
    var tafsirLoading by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("← Back", style = JannahType.bodySmall, color = JannahColors.Muted,
                modifier = Modifier.clickable(onClick = onBack))
        }

        Spacer(Modifier.height(JannahSpacing.m))

        val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
        Text(name, style = JannahType.title, color = JannahColors.Ink)

        Spacer(Modifier.height(JannahSpacing.l))

        when {
            loading -> Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
            error != null -> Text(error!!, style = JannahType.bodySmall,
                color = JannahColors.Muted, textAlign = TextAlign.Center)
            else -> ayahs.forEach { a ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.m),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(a.arabic, style = JannahType.arabic, color = JannahColors.Ink,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("(${a.number}) ${a.english}",
                        style = JannahType.bodySmall, color = JannahColors.Body,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        if (tafsirAyah == a.number) "hide tafsir" else "tap for tafsir",
                        style = JannahType.caption, color = JannahColors.Orange,
                        modifier = Modifier.clickable {
                            tafsirAyah = if (tafsirAyah == a.number) null else a.number
                        }
                    )
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
                                    Text("Loading tafsir...", style = JannahType.caption, color = JannahColors.Muted)
                                } else if (tafsirText != null) {
                                    Text(tafsirText!!, style = JannahType.bodySmall,
                                        color = JannahColors.Body, textAlign = TextAlign.Left, lineHeight = 22.sp)
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
    }
}
