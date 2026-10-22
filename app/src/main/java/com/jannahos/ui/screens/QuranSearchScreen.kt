package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.jannahos.HadithStore
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
    val listState = rememberLazyListState()

    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf("quran") }
    var hits by remember { mutableStateOf<List<QuranSearch.Hit>>(emptyList()) }
    var hadithHits by remember { mutableStateOf<List<HadithStore.Hadith>>(emptyList()) }
    var semanticHits by remember { mutableStateOf<List<QuranSearchEngine.SearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    
    var bookmarkTick by remember { mutableStateOf(0) }
    val ignore = bookmarkTick

    LaunchedEffect(query, searchMode) {
        val q = query.trim()
        if (q.length < 2) {
            hits = emptyList()
            hadithHits = emptyList()
            semanticHits = emptyList()
            searched = false
            return@LaunchedEffect
        }
        
        loading = true
        delay(400)

        if (searchMode == "hadith") {
            hadithHits = withContext(Dispatchers.IO) { HadithStore.searchAsync(ctx, q) }
        } else {
            val semantic = QuranSearchEngine.search(ctx, q)
            semanticHits = semantic
            val res = withContext(Dispatchers.IO) { try { QuranSearch.search(q) } catch (e: Exception) { null } }
            hits = res ?: emptyList()
        }
        
        loading = false
        searched = true
    }

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Search", onBack = onBack) // Pinned at the top

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.m))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by name, story, or keyword", fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(JannahSpacing.m))
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

        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = JannahSpacing.xxl)) {
            if (loading) {
                item { Text("Searching...", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.padding(JannahSpacing.gutter)) }
            } else if (searched && hits.isEmpty() && semanticHits.isEmpty() && hadithHits.isEmpty()) {
                item { Text("No matches found.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(JannahSpacing.l)) }
            } else if (searchMode == "hadith" && hadithHits.isNotEmpty()) {
                items(hadithHits.size) { i ->
                    val h = hadithHits[i]
                    Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
                        Text(h.source, style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(h.english, style = JannahType.bodySmall, color = JannahColors.Ink, lineHeight = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Narrator: ${h.narrator} · ${h.grade}", style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                }
            } else if (searchMode == "quran") {
                if (semanticHits.isNotEmpty()) {
                    item { Text("STORIES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)) }
                    items(semanticHits.size) { i ->
                        val res = semanticHits[i]
                        Row(Modifier.fillMaxWidth().pressClickable { onNavigateToPage(res.page) }.padding(horizontal = JannahSpacing.gutter, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(res.name, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                                Text("Open Page ${res.page}", style = JannahType.caption, color = JannahColors.Muted)
                            }
                            Text("→", style = JannahType.title, color = JannahColors.Orange)
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                    }
                }
                if (hits.isNotEmpty()) {
                    item { Text("VERSES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)) }
                    items(hits.size) { i ->
                        val h = hits[i]
                        val isBookmarked = bookmarks.isBookmarked(h.surah, h.ayah)
                        Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
                            Text("${h.surahName} · ${h.surah}:${h.ayah}", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            if (h.arabic.isNotBlank()) {
                                Text(stripArabicDots(h.arabic), fontFamily = UthmanicHafs, fontSize = 24.sp, color = JannahColors.Ink, textAlign = TextAlign.End, lineHeight = 40.sp, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(4.dp))
                            }
                            if (h.english.isNotBlank()) {
                                Text(h.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                            }
                            Spacer(Modifier.height(JannahSpacing.s))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.clip(RoundedCornerShape(50)).background(if (isBookmarked) JannahColors.Orange else JannahColors.Card).pressClickable {
                                    if (isBookmarked) bookmarks.remove(h.surah, h.ayah) else bookmarks.add(h.surah, h.ayah)
                                    bookmarkTick++
                                }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(if (isBookmarked) "Saved" else "Save", style = JannahType.micro, color = if (isBookmarked) JannahColors.OnOrange else JannahColors.Muted)
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

// Ultra-aggressive dot remover - strips all diacritics, quranic marks, and arabic numbers
private fun stripArabicDots(text: String): String {
    val pattern = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u08D3-\\u08FF\\u06DD\\u06DE\\u06E9\\u0660-\\u0669\\u06F0-\\u06F9]")
    return text.replace(pattern, "").replace(Regex("\\s+"), " ").trim()
}
