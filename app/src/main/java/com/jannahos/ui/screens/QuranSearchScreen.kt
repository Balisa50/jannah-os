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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import com.jannahos.QuranBookmarkStore
import com.jannahos.QuranCache
import com.jannahos.QuranSearch
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuranSearchScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val bookmarks = remember { QuranBookmarkStore(ctx) }
    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<QuranSearch.Hit>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var bookmarkTick by remember { mutableStateOf(0) }
    val ignore = bookmarkTick

    LaunchedEffect(query) {
        if (query.length < 2) {
            hits = emptyList()
            searched = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        val res = withContext(Dispatchers.IO) {
            try { QuranSearch.search(query.trim()) } catch (e: Exception) { null }
        }
        if (res == null) {
            error = "No connection. Try again when online."
            hits = emptyList()
        } else {
            hits = res
        }
        loading = false
        searched = true
    }

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
                Text("SEARCH THE QURAN",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Find a verse. ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("Any verse.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Search in English or Arabic. Both translations load.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search", fontSize = 12.sp) },
                placeholder = { Text("e.g. mercy, patience, رحمة", fontSize = 13.sp) }
            )

            Spacer(Modifier.height(JannahSpacing.l))

            when {
                loading -> Text("Searching...",
                    style = JannahType.bodySmall, color = JannahColors.Muted)
                error != null -> Text(error!!,
                    style = JannahType.bodySmall, color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                searched && hits.isEmpty() -> Text("No matches. Try a different word.",
                    style = JannahType.bodySmall, color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                hits.isNotEmpty() -> {
                    Text("${hits.size} RESULT${if (hits.size == 1) "" else "S"}",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))

                    hits.forEachIndexed { i, h ->
                        val isBookmarked = bookmarks.isBookmarked(h.surah, h.ayah)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                                .background(JannahColors.Card)
                                .padding(JannahSpacing.l)
                        ) {
                            Column {
                                Text("${h.surahName} · ${h.surah}:${h.ayah}",
                                    style = JannahType.micro,
                                    color = JannahColors.OrangeDeep,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(JannahSpacing.s))
                                if (h.arabic.isNotBlank()) {
                                    Text(h.arabic,
                                        style = JannahType.arabic,
                                        color = JannahColors.Ink,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth())
                                    Spacer(Modifier.height(JannahSpacing.s))
                                }
                                if (h.english.isNotBlank()) {
                                    Text(h.english,
                                        style = JannahType.bodySmall,
                                        color = JannahColors.Body,
                                        lineHeight = 22.sp)
                                }
                                Spacer(Modifier.height(JannahSpacing.m))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                        .background(
                                            if (isBookmarked) JannahColors.Orange else JannahColors.Cream
                                        )
                                        .clickable {
                                            if (isBookmarked) bookmarks.remove(h.surah, h.ayah)
                                            else bookmarks.add(h.surah, h.ayah)
                                            bookmarkTick++
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        if (isBookmarked) "★ saved" else "☆ save",
                                        style = JannahType.caption,
                                        color = if (isBookmarked) JannahColors.OnOrange else JannahColors.Muted
                                    )
                                }
                            }
                        }
                        if (i < hits.size - 1) Spacer(Modifier.height(JannahSpacing.s))
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
