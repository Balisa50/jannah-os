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
    val listState = rememberLazyListState()

    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf("quran") }
    var quranHits by remember { mutableStateOf<List<QuranSearchEngine.AyahResult>>(emptyList()) }
    var hadithHits by remember { mutableStateOf<List<QuranSearchEngine.HadithResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }

    LaunchedEffect(query, searchMode) {
        val q = query.trim()
        if (q.length < 2) {
            quranHits = emptyList()
            hadithHits = emptyList()
            searched = false
            return@LaunchedEffect
        }
        loading = true
        delay(400) // Debounce

        withContext(Dispatchers.IO) {
            if (searchMode == "hadith") {
                val res = QuranSearchEngine.searchHadith(ctx, q)
                withContext(Dispatchers.Main) {
                    hadithHits = res
                    quranHits = emptyList()
                }
            } else {
                val res = QuranSearchEngine.searchQuran(ctx, q)
                withContext(Dispatchers.Main) {
                    quranHits = res
                    hadithHits = emptyList()
                }
            }
        }
        loading = false
        searched = true
    }

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Search", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.m))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Quran or Hadith", fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(JannahSpacing.m))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(50)).background(if (searchMode == "quran") JannahColors.Orange else JannahColors.Card).pressClickable { searchMode = "quran" }, contentAlignment = Alignment.Center) { Text("Quran", color = if (searchMode == "quran") JannahColors.OnOrange else JannahColors.Ink, fontWeight = FontWeight.Bold) }
                Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(50)).background(if (searchMode == "hadith") JannahColors.Orange else JannahColors.Card).pressClickable { searchMode = "hadith" }, contentAlignment = Alignment.Center) { Text("Hadith", color = if (searchMode == "hadith") JannahColors.OnOrange else JannahColors.Ink, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(JannahSpacing.l))
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = JannahSpacing.xxl)) {
            if (loading) {
                item { Text("Searching...", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.padding(JannahSpacing.gutter)) }
            } else if (searched && quranHits.isEmpty() && hadithHits.isEmpty()) {
                item { Text("No matches found.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(JannahSpacing.l)) }
            } else if (searchMode == "hadith" && hadithHits.isNotEmpty()) {
                items(hadithHits.size) { i ->
                    val h = hadithHits[i]
                    Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
                        Text("${h.collection.uppercase()} · ${h.number}", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        if (h.arabic.isNotBlank()) {
                            Text(stripAllArabicMarks(h.arabic), fontFamily = UthmanicHafs, fontSize = 22.sp, color = JannahColors.Ink, textAlign = TextAlign.End, lineHeight = 36.sp, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            "BUNDLED HADITH EXCERPT · EDITION AND GRADING NOT VERIFIED",
                            style = JannahType.micro,
                            color = JannahColors.OrangeDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(h.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                }
            } else if (searchMode == "quran" && quranHits.isNotEmpty()) {
                items(quranHits.size) { i ->
                    val a = quranHits[i]
                    Column(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.m)) {
                        Text("Surah ${a.surah} · Ayah ${a.ayah}", style = JannahType.micro, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(stripAllArabicMarks(a.arabic), fontFamily = UthmanicHafs, fontSize = 24.sp, color = JannahColors.Ink, textAlign = TextAlign.End, lineHeight = 40.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "BUNDLED ENGLISH TRANSLATION · TRANSLATOR AND EDITION NOT IDENTIFIED",
                            style = JannahType.micro,
                            color = JannahColors.OrangeDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(a.english, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
                }
            }
        }
    }
}

// ULTIMATE DOT REMOVER: Keeps only standard Arabic letters and spaces
private fun stripAllArabicMarks(text: String): String {
    val pattern = Regex("[^\\u0621-\\u064A\\s]")
    return text.replace(pattern, "").replace(Regex("\\s+"), " ").trim()
}
