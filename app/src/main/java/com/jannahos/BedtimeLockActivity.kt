package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme
import kotlinx.coroutines.delay

class BedtimeLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                val ctx = LocalContext.current
                val pagerState = rememberPagerState(initialPage = 561, pageCount = { 564 }) // Surah Al-Mulk is pages 562-564
                var remaining by remember { mutableLongStateOf(5 * 60_000L) }

                LaunchedEffect(Unit) {
                    while (remaining > 0L) {
                        delay(1000)
                        remaining -= 1000L
                    }
                    finish()
                }

                val mm = (remaining / 60_000L).toInt()
                val ss = ((remaining / 1000L) % 60).toInt()

                Box(Modifier.fillMaxSize().background(Color.White)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        val bitmap = remember(page) { MushafImageStore.loadBitmap(ctx, page + 1) }
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                        }
                    }
                    Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(JannahColors.Orange).padding(16.dp)) {
                        Text("Read Surah Al-Mulk - ${String.format("%02d:%02d", mm, ss)}", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
