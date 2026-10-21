package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
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
import com.jannahos.QuranBookmarkStore
import com.jannahos.QuranSearch
import com.jannahos.QuranSearchEngine
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val UthmanicHafs = FontFamily(Font(com.jannahos.R.font.uthmanic_hafs))

@Composable
fun QuranSearchScreen(onBack: () -> Unit, onNavigateToPage: (Int) -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val bookmarks = remember { QuranBookmarkStore(ctx) }

    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf("quran") } // "quran" or "hadith"
    var hadithHits by remember { mutableStateOf<List<com.jannahos.HadithStore.Hadith>>(emptyList()) }
    var hits by remember { mutableStateOf<List<QuranSearch.Hit>>(emptyList()) }
    var semanticHits by remember { mutableStateOf<List<QuranSearchEngine.SearchResult>>(emptyList()) }
    var semanticVerses by remember { mutableStateOf<Map<Int, List<QuranApi.Ayah>>>(emptyMap()) } // SurahId -> Ayahs
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var bookmarkTick by remember { mutableStateOf(0) }
    val ignore = bookmarkTick

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            hits = emptyList()
            semanticHits = emptyList()
            semanticVerses = emptyMap()
            searched = false
            return@LaunchedEffect
        }
        
        loading = true
        error = null
        delay(400) // Debounce

        if (searchMode == "hadith") {
            hadithHits = com.jannahos.HadithStore.searchAsync(ctx, q)
            loading = false
            searched = true
            return@LaunchedEffect
        }

        // 1. Check Semantic Engine
        val semantic = QuranSearchEngine.search(ctx, q)
        semanticHits = semantic

        // 2. Fetch text for semantic hits
        val verseMap = mutableMapOf<Int, List<QuranApi.Ayah>>()
        for (res in semantic) {
            val surahText = withContext(Dispatchers.IO) {
                try { QuranApi.fetchSurah(res.surahId) } catch (e: Exception) { emptyList() }
            }
            if (surahText.isNotEmpty()) {
                val filtered = surahText.filter { it.number in res.ayahStart..res.ayahEnd }
                verseMap[res.surahId] = if (filtered.isNotEmpty()) filtered else surahText.take(5)
            }
        }
        semanticVerses = verseMap

        // 3. Check Text Search API
        val res = withContext(Dispatchers.IO) {
            try { QuranSearch.search(q) } catch (e: Exception) { null }
        }
        
        if (res == null && semantic.isEmpty()) {
            error = "Check your connection."
        } else {
            hits = res ?: emptyList()
        }
        
        loading = false
        searched = true
    }

    Column(
        modifier = Modifier.fillMaxSize().background(JannahColors.Cream)
    ) {
        TopBar("Search Quran", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.m))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by name, story, or keyword", fontSize = 12.sp) },
                placeholder = { Text("e.g. Musa, patience, mercy", fontSize = 13.sp) },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(JannahSpacing.m))
            
            // Toggle
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(50))
                        .background(if (searchMode == "quran") JannahColors.Orange else JannahColors.Card)
                        .pressClickable { searchMode = "quran" },
                    contentAlignment = Alignment.Center
                ) { Text("Quran", color = if (searchMode == "quran") JannahColors.OnOrange else JannahColors.Ink, fontWeight = FontWeight.Bold) }
                
                Box(
                    Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(50))
                        .background(if (searchMode == "hadith") JannahColors.Orange else JannahColors.Card)
                        .pressClickable { searchMode = "hadith" },
                    contentAlignment = Alignment.Center
                ) { Text("Hadith", color = if (searchMode == "hadith") JannahColors.OnOrange else JannahColors.Ink, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(JannahSpacing.l))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = JannahSpacing.xxl)
        ) {
            if (loading) {
                item {
                    Text("Searching...", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.padding(JannahSpacing.gutter))
                }
            } else if (error != null && hits.isEmpty() && semanticHits.isEmpty()) {
                item {
                    Text(error!!, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(JannahSpacing.l))
                }
            } else if (searched && hits.isEmpty() && semanticHits.isEmpty()) {
                item {
                    Text("No matches found. Try a different word.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(JannahSpacing.l))
                }
            } else {
                // 1. Semantic Matches with Text
                if (semanticHits.isNotEmpty()) {
                    item {
                        Text("STORIES & SCENARIOS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s))
                    }
                    items(semanticHits.size) { i ->
                        val res = semanticHits[i]
                        val verses = semanticVerses[res.surahId] ?: emptyList()
                        
                        Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)) {
                            Row(
                                Modifier.fillMaxWidth().pressClickable { onNavigateToPage(res.page) }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(res.name, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                                    Text("Page ${res.page} · Tap to read full context", style = JannahType.caption, color = JannahColors.Orange)
                                }
                                Text("→", style = JannahType.title, color = JannahColors.Orange)
                            }
                            
                            // Render the text
                            verses.forEach { ayah ->
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    Text(cleanArabic(ayah.arabic), fontFamily = UthmanicHafs, fontSize = 24.sp, lineHeight = 40.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(), color = JannahColors.Ink)
                                    Spacer(Modifier.height(4.dp))
                                    Text(ayah.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                    }
                }

                // 2. Text API Matches
                if (hits.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(JannahSpacing.m))
                        Text("VERSES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s))
                    }
                    items(hits.size) { i ->
                        val h = hits[i]
                        val isBookmarked = bookmarks.isBookmarked(h.surah, h.ayah)
                        
                        Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
                            Text("${h.surahName} · ${h.surah}:${h.ayah}", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(JannahSpacing.xs))
                            
                            // Always fetch the Arabic text from the local store using the Surah/Ayah
                            val localAyat = remember(h.surah, h.ayah) {
                                com.jannahos.QuranTextStore.getAyah(ctx, h.surah, h.ayah)
                            }
                            val arabicText = localAyat?.text ?: h.arabic
                            
                            if (arabicText.isNotBlank()) {
                                Text(cleanArabic(arabicText), fontFamily = UthmanicHafs, fontSize = 24.sp, color = JannahColors.Ink, textAlign = TextAlign.End, lineHeight = 40.sp, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(JannahSpacing.xs))
                            }
                            if (h.english.isNotBlank()) {
                                Text(h.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                            }
                            
                            Spacer(Modifier.height(JannahSpacing.s))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier.clip(RoundedCornerShape(50)).background(if (isBookmarked) JannahColors.Orange else JannahColors.Card).pressClickable {
                                        if (isBookmarked) bookmarks.remove(h.surah, h.ayah) else bookmarks.add(h.surah, h.ayah)
                                        bookmarkTick++
                                    }.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (isBookmarked) "★ Saved" else "☆ Save", style = JannahType.micro, color = if (isBookmarked) JannahColors.OnOrange else JannahColors.Muted)
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                    }
                }
            }
        }
    }
}


private fun cleanArabic(text: String): String {
    // Removes Ayah markers (U+06DD) and Arabic numerals (U+0660 to U+0669, U+06F0 to U+06F9)
    return text.replace(Regex("[\\u06DD\\u06DE\\u06E9\\u0660-\\u0669\\u06F0-\\u06F9]"), "").trim()
}
