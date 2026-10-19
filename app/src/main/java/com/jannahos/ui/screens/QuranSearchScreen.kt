package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun QuranSearchScreen(onBack: () -> Unit, onNavigateToSurah: (Int) -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val bookmarks = remember { QuranBookmarkStore(ctx) }

    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<QuranSearch.Hit>>(emptyList()) }
    var semanticHits by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
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
            searched = false
            return@LaunchedEffect
        }
        
        loading = true
        error = null
        delay(400) // Debounce

        // 1. Check Semantic Engine (Scenario Matching)
        val semantic = QuranSearchEngine.search(q)
        semanticHits = semantic

        // 2. Check Text Search API (if it's a specific word)
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
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
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
                // 1. Show Semantic Story Matches First
                if (semanticHits.isNotEmpty()) {
                    item {
                        Text("STORIES & SCENARIOS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s))
                    }
                    items(semanticHits.size) { i ->
                        val (surahId, surahName) = semanticHits[i]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .pressClickable { onNavigateToSurah(surahId) }
                                .padding(horizontal = JannahSpacing.gutter, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(surahName, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                                Text("Go to Surah", style = JannahType.caption, color = JannahColors.Muted)
                            }
                            Text("→", style = JannahType.title, color = JannahColors.Orange)
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                    }
                }

                // 2. Show Text API Matches
                if (hits.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(JannahSpacing.m))
                        Text("VERSES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s))
                    }
                    items(hits.size) { i ->
                        val h = hits[i]
                        val isBookmarked = bookmarks.isBookmarked(h.surah, h.ayah)
                        
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)
                        ) {
                            Text(
                                "${h.surahName} · ${h.surah}:${h.ayah}",
                                style = JannahType.micro,
                                color = JannahColors.OrangeDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(JannahSpacing.xs))
                            
                            if (h.arabic.isNotBlank()) {
                                Text(
                                    h.arabic,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    color = JannahColors.Ink,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(JannahSpacing.xs))
                            }
                            if (h.english.isNotBlank()) {
                                Text(h.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                            }
                            
                            Spacer(Modifier.height(JannahSpacing.s))
                            
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isBookmarked) JannahColors.Orange else JannahColors.Card)
                                    .pressClickable {
                                        if (isBookmarked) bookmarks.remove(h.surah, h.ayah)
                                        else bookmarks.add(h.surah, h.ayah)
                                        bookmarkTick++
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    if (isBookmarked) "★ Saved" else "☆ Save",
                                    style = JannahType.micro,
                                    color = if (isBookmarked) JannahColors.OnOrange else JannahColors.Muted
                                )
                            }
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                    }
                }
            }
        }
    }
}
