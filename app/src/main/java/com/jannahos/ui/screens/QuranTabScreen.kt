package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranApi
import com.jannahos.QuranBookmarks
import com.jannahos.QuranCache
import com.jannahos.TafsirApi
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahBrushes
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
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Quran", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Uthmani text · Sahih International",
            style = JannahType.caption, color = JannahColors.Muted)

        if (last != null) {
            Spacer(Modifier.height(JannahSpacing.l))
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(last) }
                        .padding(JannahSpacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("CONTINUE", style = JannahType.eyebrow, color = JannahColors.Muted)
                        Spacer(Modifier.height(JannahSpacing.xs))
                        val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                        Text(name, style = JannahType.body, color = JannahColors.Ivory,
                            fontSize = 18.sp)
                    }
                    Text("→", style = JannahType.body, color = JannahColors.Gold)
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text("SURAHS", style = JannahType.eyebrow, color = JannahColors.Muted,
            modifier = Modifier.padding(horizontal = JannahSpacing.xs))
        Spacer(Modifier.height(JannahSpacing.s))

        GlassCard {
            Column(Modifier.padding(vertical = JannahSpacing.xs)) {
                QuranCache.SURAHS.forEachIndexed { idx, (num, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(num) }
                            .padding(horizontal = JannahSpacing.m, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            num.toString(),
                            style = JannahType.caption,
                            color = JannahColors.Faint,
                            modifier = Modifier.fillMaxWidth(0.12f)
                        )
                        Text(
                            name,
                            style = JannahType.body,
                            color = JannahColors.Ivory,
                            modifier = Modifier.weight(1f)
                        )
                        Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                    }
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))
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
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("← Back", style = JannahType.bodySmall, color = JannahColors.Muted,
            modifier = Modifier.clickable(onClick = onBack))

        Spacer(Modifier.height(JannahSpacing.l))

        val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
        Text(name, style = JannahType.title, color = JannahColors.Ivory)

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
                    Text(a.arabic, style = JannahType.arabic, color = JannahColors.Ivory,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("(${a.number}) ${a.english}",
                        style = JannahType.bodySmall, color = JannahColors.Body,
                        textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        if (tafsirAyah == a.number) "hide tafsir" else "tap for tafsir",
                        style = JannahType.caption, color = JannahColors.Gold,
                        modifier = Modifier.clickable {
                            tafsirAyah = if (tafsirAyah == a.number) null else a.number
                        }
                    )

                    if (tafsirAyah == a.number) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        GlassCard {
                            Column(Modifier.padding(JannahSpacing.m)) {
                                if (tafsirLoading) {
                                    Text("Loading tafsir...",
                                        style = JannahType.caption, color = JannahColors.Muted)
                                } else if (tafsirText != null) {
                                    Text(tafsirText!!, style = JannahType.bodySmall,
                                        color = JannahColors.Body,
                                        textAlign = TextAlign.Left, lineHeight = 22.sp)
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
