package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    QuranScreen()
                }
            }
        }
    }

    @Composable
    private fun QuranScreen() {
        var selected by remember { mutableStateOf<Int?>(null) }
        if (selected == null) SurahList(onPick = { selected = it })
        else SurahReader(surah = selected!!, onBack = { selected = null })
    }

    @Composable
    private fun SurahList(onPick: (Int) -> Unit) {
        val last = remember { QuranBookmarks.lastSurah(this@QuranActivity) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Quran", style = JannahType.screenTitle, color = JannahColors.DayText)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("Uthmani text · Sahih International", style = JannahType.tertiary, color = JannahColors.DayMuted)

            if (last != null) {
                Spacer(Modifier.height(JannahSpacing.l))
                Button(
                    onClick = { onPick(last) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.Accent,
                        contentColor = JannahColors.AccentInk
                    )
                ) {
                    val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                    Text("Continue: $name", style = JannahType.button)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            QuranCache.SURAHS.forEach { (num, name) ->
                Button(
                    onClick = { onPick(num) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.DaySurface,
                        contentColor = JannahColors.DayText
                    )
                ) {
                    Text("$num. $name", style = JannahType.secondary,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }

    @Composable
    private fun SurahReader(surah: Int, onBack: () -> Unit) {
        val cache = remember { QuranCache(this) }
        var ayahs by remember { mutableStateOf<List<QuranApi.Ayah>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var tafsirAyah by remember { mutableStateOf<Int?>(null) }
        var tafsirText by remember { mutableStateOf<String?>(null) }
        var tafsirLoading by remember { mutableStateOf(false) }

        LaunchedEffect(surah) {
            loading = true
            QuranBookmarks.setLastSurah(this@QuranActivity, surah)
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
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.l))

            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) {
                    Text("Back", style = JannahType.secondary, color = JannahColors.DayMuted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.s))

            val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
            Text(name, style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.l))

            when {
                loading -> Text("Loading...", style = JannahType.secondary, color = JannahColors.DayMuted)
                error != null -> Text(error!!, style = JannahType.secondary,
                    color = JannahColors.DayMuted, textAlign = TextAlign.Center)
                else -> ayahs.forEach { a ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.m),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(a.arabic, style = JannahType.arabic, color = JannahColors.DayText,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "(${a.number}) ${a.english}",
                            style = JannahType.secondary,
                            color = JannahColors.DayMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "tap for tafsir",
                            style = JannahType.tertiary,
                            color = JannahColors.Accent,
                            modifier = Modifier.clickable { tafsirAyah = a.number }
                        )

                        if (tafsirAyah == a.number) {
                            Spacer(Modifier.height(JannahSpacing.s))
                            if (tafsirLoading) {
                                Text("Loading tafsir...", style = JannahType.tertiary, color = JannahColors.DayMuted)
                            } else if (tafsirText != null) {
                                Text(tafsirText!!, style = JannahType.secondary, color = JannahColors.DayText,
                                    textAlign = TextAlign.Left, lineHeight = 20.sp)
                                Spacer(Modifier.height(JannahSpacing.xs))
                                Text("Ibn Kathir (abridged) · via alquran.cloud",
                                    style = JannahType.tertiary, color = JannahColors.DayMuted)
                            } else {
                                Text("Tafsir unavailable (offline).",
                                    style = JannahType.tertiary, color = JannahColors.DayMuted)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
