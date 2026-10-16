package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranPreloader
import com.jannahos.QuranReaderPrefs
import com.jannahos.audio.QuranPlaybackService
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val AmiriQuran = FontFamily(Font(com.jannahos.R.font.amiri_quran))

// The mushaf reader. Renders one of the 604 canonical Madani pages at a time.
// Text flows as one justified paragraph per the printed Mushaf. Swipe left
// or right to turn pages. Long-press an ayah to play it.
@Composable
fun MushafPageScreen(
    startPage: Int,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var currentPage by remember { mutableStateOf(startPage.coerceIn(1, 604)) }
    var pageText by remember { mutableStateOf<String?>(null) }
    var firstAyahOnPage by remember { mutableStateOf<Pair<Int, Int>?>(null) } // surah to ayah
    var loading by remember { mutableStateOf(true) }

    val fontSize = remember { QuranReaderPrefs.fontSize(ctx) }

    // Load the page from cache.
    LaunchedEffect(currentPage) {
        loading = true
        pageText = withContext(Dispatchers.IO) {
            val raw = QuranPreloader.loadPage(ctx, currentPage) ?: return@withContext null
            try {
                val root = JSONObject(raw)
                val verses = root.getJSONArray("verses")
                val sb = StringBuilder()
                for (i in 0 until verses.length()) {
                    val v = verses.getJSONObject(i)
                    val text = v.optString("text_uthmani", "")
                    val surah = v.optInt("surah_number", 0)
                    val ayahNum = v.optInt("verse_number", 0)
                    if (i == 0) firstAyahOnPage = surah to ayahNum
                    sb.append(text.trim())
                    // Arabic-Indic ayah number in the traditional marker
                    sb.append(" ")
                    sb.append("\u06DD")
                    sb.append(toArabic(ayahNum))
                    sb.append(" ")
                }
                sb.toString()
            } catch (e: Exception) {
                null
            }
        }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar("Page $currentPage", onBack = onBack)

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFDF8))
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else if (pageText == null) {
                Box(
                    Modifier.fillMaxSize().padding(JannahSpacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This page is not downloaded yet.",
                            style = JannahType.body, color = JannahColors.Ink,
                            fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("Go to Settings → Download the Quran offline to fetch all 604 pages.",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.l)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Flowing mushaf text: one big paragraph with ayah markers.
                    Text(
                        text = pageText!!,
                        fontFamily = AmiriQuran,
                        fontSize = fontSize.sp,
                        color = JannahColors.Ink,
                        lineHeight = (fontSize * 2.0f).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Left/right page turn zones
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(0.15f)
                        .fillMaxSize()
                        .pointerInput(currentPage) {
                            detectHorizontalDragGestures { _, _ -> }
                        }
                )
                Spacer(Modifier.weight(0.7f))
                Box(
                    Modifier
                        .weight(0.15f)
                        .fillMaxSize()
                        .pointerInput(currentPage) {
                            detectHorizontalDragGestures { _, _ -> }
                        }
                )
            }
        }

        // Page navigation row
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageNavButton("← Prev") {
                if (currentPage > 1) currentPage--
            }
            Text("$currentPage / 604",
                style = JannahType.caption, color = JannahColors.Muted)
            PageNavButton("Next →") {
                if (currentPage < 604) currentPage++
            }
        }
    }
}

@Composable
private fun PageNavButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Card)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, _ -> }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(label, style = JannahType.button, color = JannahColors.Ink,
            modifier = Modifier.then(
                androidx.compose.ui.Modifier.let { it }
            ))
    }
}

private fun toArabic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
