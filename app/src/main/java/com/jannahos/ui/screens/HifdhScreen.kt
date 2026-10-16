package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
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
import com.jannahos.HifdhLog
import com.jannahos.QuranApi
import com.jannahos.QuranCache
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HifdhScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    val log = remember { HifdhLog(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    var selectedSurah by remember { mutableStateOf<Int?>(null) }

    if (selectedSurah == null) {
        HifdhIndex(onBack = onBack, onPick = { selectedSurah = it })
    } else {
        HifdhSurah(surah = selectedSurah!!, onBack = { selectedSurah = null })
    }
}

@Composable
private fun HifdhIndex(onBack: () -> Unit, onPick: (Int) -> Unit) {
    val ctx = LocalContext.current
    val log = remember { HifdhLog(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val due = log.dueNow()
    val total = log.totalMemorised()

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
                Text("HIFDH",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("$total ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("ayah memorised.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (due.isNotEmpty()) "${due.size} ayah due for revision"
                    else "Nothing due right now",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Due now
            if (due.isNotEmpty()) {
                Text("DUE NOW", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        val bySurah = due.groupBy { it.surah }
                        bySurah.entries.take(5).forEach { (surah, entries) ->
                            val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second
                                ?: "Surah $surah"
                            val ayahList = entries.map { it.ayah }.sorted()
                            Text(
                                "$name · ${ayahList.size} ayah (${ayahList.first()}–${ayahList.last()})",
                                style = JannahType.bodySmall,
                                color = JannahColors.Ink,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        if (bySurah.size > 5) {
                            Text("+ ${bySurah.size - 5} more surahs",
                                style = JannahType.caption, color = JannahColors.Muted)
                        }
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.Ink)
                                .clickable { onPick(due.first().surah) }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text("Revise now", style = JannahType.button,
                                color = JannahColors.OnOrange)
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            Text("PICK A SURAH",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            QuranCache.SURAHS.forEachIndexed { i, (num, name) ->
                val memorised = log.memorisedInSurah(num)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(num) }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$num", style = JannahType.caption,
                        color = JannahColors.Faint, modifier = Modifier.width(36.dp))
                    Text(name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    if (memorised > 0) {
                        Text("$memorised",
                            style = JannahType.caption,
                            color = JannahColors.Orange,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < QuranCache.SURAHS.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
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

@Composable
private fun HifdhSurah(surah: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val log = remember { HifdhLog(ctx) }
    val cache = remember { QuranCache(ctx) }
    var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    var reviewMode by remember { mutableStateOf(false) }
    var reviewIndex by remember { mutableStateOf(0) }
    var showArabic by remember { mutableStateOf(false) }

    LaunchedEffect(surah) {
        loading = true
        ayahs = withContext(Dispatchers.IO) {
            try {
                if (cache.isCached(surah)) cache.load(surah)
                else {
                    val fetched = QuranApi.fetchSurah(surah)
                    cache.save(surah, fetched)
                    fetched
                }
            } catch (e: Exception) { emptyList() }
        }
        loading = false
    }

    val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
    val dueForSurah = remember { log.dueNow().filter { it.surah == surah } }

    if (reviewMode && ayahs.isNotEmpty()) {
        val due = dueForSurah
        val idx = reviewIndex.coerceIn(0, (due.size - 1).coerceAtLeast(0))
        val e = due.getOrNull(idx)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(JannahColors.Cream)
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            Spacer(Modifier.height(JannahSpacing.l))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("← Exit review", style = JannahType.bodySmall, color = JannahColors.Muted,
                    modifier = Modifier.clickable { reviewMode = false })
                Spacer(Modifier.weight(1f))
                Text("${idx + 1} of ${due.size}",
                    style = JannahType.caption, color = JannahColors.Muted)
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            if (e == null) {
                Text("No more due in this surah.",
                    style = JannahType.body, color = JannahColors.Muted)
            } else {
                val ayah = ayahs.firstOrNull { it.number == e.ayah }
                Text("$name · ayah ${e.ayah}",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.m))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .clickable { showArabic = !showArabic }
                        .padding(JannahSpacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (showArabic && ayah != null) ayah.arabic
                            else "tap to reveal Arabic",
                            style = if (showArabic) JannahType.arabic else JannahType.body,
                            color = if (showArabic) JannahColors.Ink else JannahColors.Muted,
                            textAlign = TextAlign.Center
                        )
                        if (showArabic && ayah != null) {
                            Spacer(Modifier.height(JannahSpacing.m))
                            Text(ayah.english,
                                style = JannahType.bodySmall,
                                color = JannahColors.Body,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(JannahSpacing.l))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Card)
                            .clickable {
                                log.review(e.surah, e.ayah, well = false)
                                showArabic = false
                                if (idx + 1 >= due.size) reviewMode = false
                                else reviewIndex = idx + 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Needs work", style = JannahType.button, color = JannahColors.Ink)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Orange)
                            .clickable {
                                log.review(e.surah, e.ayah, well = true)
                                showArabic = false
                                if (idx + 1 >= due.size) reviewMode = false
                                else reviewIndex = idx + 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Solid", style = JannahType.button, color = JannahColors.OnOrange)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
        return
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
                Text("HIFDH · SURAH ${surah}",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text(name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("${log.memorisedInSurah(surah)} of ${ayahs.size} memorised",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (dueForSurah.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable {
                            reviewIndex = 0
                            showArabic = false
                            reviewMode = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Revise ${dueForSurah.size} due ayah",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            if (loading) {
                Text("Loading...", style = JannahType.bodySmall, color = JannahColors.Muted)
            } else if (ayahs.isEmpty()) {
                Text("Could not load this surah. Connect and try again.",
                    style = JannahType.bodySmall, color = JannahColors.Muted)
            } else {
                ayahs.forEachIndexed { i, a ->
                    val memorised = log.isMemorised(surah, a.number)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Ayah ${a.number}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                a.english.take(60) + if (a.english.length > 60) "…" else "",
                                style = JannahType.caption,
                                color = JannahColors.Muted
                            )
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(if (memorised) JannahColors.Orange else JannahColors.Card)
                                .clickable {
                                    if (memorised) log.unmark(surah, a.number)
                                    else log.mark(surah, a.number)
                                    refresh = refresh + 1
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                if (memorised) "✓ Memorised" else "Mark",
                                style = JannahType.caption,
                                color = if (memorised) JannahColors.OnOrange else JannahColors.Muted
                            )
                        }
                    }
                    if (i < ayahs.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
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
