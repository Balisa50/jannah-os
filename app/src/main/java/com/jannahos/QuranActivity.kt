package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val NightBg = Color(0xFF0E1116)
private val NightSurface = Color(0xFF161A22)
private val NightText = Color(0xFFE8E5DC)
private val NightMuted = Color(0xFF8B8F98)
private val GoldAccent = Color(0xFFC9A961)

private val Colors = darkColorScheme(
    background = NightBg,
    surface = NightSurface,
    onBackground = NightText,
    onSurface = NightText,
    primary = GoldAccent,
    onPrimary = NightBg
)

class QuranActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
                    QuranScreen()
                }
            }
        }
    }

    @Composable
    private fun QuranScreen() {
        var selected by remember { mutableStateOf<Int?>(null) }

        if (selected == null) {
            SurahList(onPick = { selected = it })
        } else {
            SurahReader(surah = selected!!, onBack = { selected = null })
        }
    }

    @Composable
    private fun SurahList(onPick: (Int) -> Unit) {
        val last = remember { QuranBookmarks.lastSurah(this@QuranActivity) }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            Text("Quran", color = NightText, fontSize = 28.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Text("Uthmani text · Sahih International", color = NightMuted, fontSize = 12.sp)

            if (last != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onPick(last) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
                ) {
                    val name = QuranCache.SURAHS.firstOrNull { it.first == last }?.second ?: "Surah $last"
                    Text("Continue: $name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(36.dp))

            QuranCache.SURAHS.forEach { (num, name) ->
                Button(
                    onClick = { onPick(num) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
                ) {
                    Text("$num. $name", fontSize = 15.sp, modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left)
                }
            }
            Spacer(Modifier.height(40.dp))
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            TextButton(onClick = onBack) {
                Text("Back", color = NightMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))

            val name = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second ?: "Surah $surah"
            Text(name, color = NightText, fontSize = 22.sp, fontWeight = FontWeight.Light)

            Spacer(Modifier.height(28.dp))

            when {
                loading -> Text("Loading...", color = NightMuted, fontSize = 14.sp)
                error != null -> Text(error!!, color = NightMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                else -> ayahs.forEach { a ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            a.arabic,
                            color = NightText,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 44.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "(${a.number}) ${a.english}",
                            color = NightMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "tap for tafsir",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                tafsirAyah = a.number
                            }
                        )

                        if (tafsirAyah == a.number) {
                            Spacer(Modifier.height(12.dp))
                            if (tafsirLoading) {
                                Text("Loading tafsir...", color = NightMuted, fontSize = 12.sp)
                            } else if (tafsirText != null) {
                                Text(
                                    tafsirText!!,
                                    color = NightText,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Left,
                                    lineHeight = 20.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Ibn Kathir (abridged) · via alquran.cloud",
                                    color = NightMuted, fontSize = 10.sp)
                            } else {
                                Text("Tafsir unavailable (offline).",
                                    color = NightMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
