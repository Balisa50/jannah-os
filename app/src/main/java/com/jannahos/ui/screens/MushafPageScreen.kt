package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranReaderPrefs
import com.jannahos.QuranStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AmiriQuran = FontFamily(Font(com.jannahos.R.font.amiri_quran))

@Composable
fun MushafPageScreen(startSurah: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var currentSurah by remember { mutableIntStateOf(startSurah.coerceIn(1, 114)) }
    var ayahs by remember { mutableStateOf<List<com.jannahos.QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var dragTotal by remember { mutableStateOf(0f) }

    val fontSize = remember { QuranReaderPrefs.fontSize(ctx) }
    val surahName = remember(currentSurah) {
        com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == currentSurah }?.second
            ?: "Surah $currentSurah"
    }

    LaunchedEffect(currentSurah) {
        loading = true
        ayahs = withContext(Dispatchers.IO) {
            if (QuranStore.isDownloaded(ctx)) {
                QuranStore.ayahsFor(ctx, currentSurah)
            } else {
                // Fall back to network while setup is incomplete
                try {
                    val fetched = com.jannahos.QuranApi.fetchSurah(currentSurah)
                    fetched
                } catch (e: Exception) { emptyList() }
            }
        }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar(surahName, onBack = onBack)

        // The mushaf page. Swipe left or right to change surah.
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFDF8))
                .pointerInput(currentSurah) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragTotal < -80f && currentSurah < 114) {
                                currentSurah++
                            } else if (dragTotal > 80f && currentSurah > 1) {
                                currentSurah--
                            }
                            dragTotal = 0f
                        },
                        onHorizontalDrag = { _, dragAmount -> dragTotal += dragAmount }
                    )
                }
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else if (ayahs.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(JannahSpacing.l), contentAlignment = Alignment.Center) {
                    Text("Could not load this surah.",
                        style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.l)
                ) {
                    // Basmalah as a separate line, except At-Tawbah.
                    if (currentSurah != 9) {
                        Text(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = AmiriQuran,
                            fontSize = (fontSize + 2).sp,
                            color = JannahColors.Ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = (fontSize * 2f).sp
                        )
                        Spacer(Modifier.height(JannahSpacing.l))
                    }

                    // Flowing mushaf text — all ayahs as one continuous paragraph.
                    Text(
                        text = buildString {
                            ayahs.forEach { a ->
                                append(a.arabic.trim())
                                append("  ۝")
                                append(toArabicIndic(a.number))
                                append(" ")
                            }
                        },
                        fontFamily = AmiriQuran,
                        fontSize = fontSize.sp,
                        color = JannahColors.Ink,
                        lineHeight = (fontSize * 2.05f).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Page-turn hint
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = JannahSpacing.l, vertical = JannahSpacing.s),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (currentSurah > 1) "← ${prevName(currentSurah)}" else "",
                    style = JannahType.micro,
                    color = JannahColors.Faint
                )
                Text(
                    if (currentSurah < 114) "${nextName(currentSurah)} →" else "",
                    style = JannahType.micro,
                    color = JannahColors.Faint
                )
            }
        }
    }
}

private fun prevName(surah: Int): String =
    com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == surah - 1 }?.second ?: ""
private fun nextName(surah: Int): String =
    com.jannahos.QuranCache.SURAHS.firstOrNull { it.first == surah + 1 }?.second ?: ""

private fun toArabicIndic(n: Int): String {
    val map = charArrayOf('\u0660','\u0661','\u0662','\u0663','\u0664','\u0665','\u0666','\u0667','\u0668','\u0669')
    return n.toString().map { c -> if (c.isDigit()) map[c - '0'] else c }.joinToString("")
}
