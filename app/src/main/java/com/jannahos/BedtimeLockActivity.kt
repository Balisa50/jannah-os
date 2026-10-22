package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

class BedtimeLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                BedtimeLockContent(onDone = { finish() })
            }
        }
    }
}

@Composable
fun BedtimeLockContent(onDone: () -> Unit) {
    val ctx = LocalContext.current
    // Surah Al-Mulk is on pages 562 to 564
    val pagerState = rememberPagerState(initialPage = 561, pageCount = { 564 })
    var remaining by remember { mutableLongStateOf(5 * 60_000L) } // 5 minutes
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(started) {
        if (started) {
            while (remaining > 0L) {
                delay(1000)
                remaining -= 1000L
            }
            onDone()
        }
    }

    val mm = (remaining / 60_000L).toInt()
    val ss = ((remaining / 1000L) % 60).toInt()

    Box(Modifier.fillMaxSize().background(Color.White)) {
        if (!started) {
            // Evidence Screen
            Column(
                Modifier.fillMaxSize().background(JannahColors.Cream).padding(JannahSpacing.gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("BEDTIME LOCK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(16.dp))
                Text("Surah Al-Mulk", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                    Text(
                        "The Prophet ﷺ said: \"Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.\" (Jami' at-Tirmidhi 2891)",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        color = JannahColors.Ink,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Orange).pressClickable { started = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Start Reading (5 mins)", style = JannahType.button, color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Reading Screen
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val bitmap = remember(page) { MushafImageStore.loadBitmap(ctx, page + 1) }
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
            
            Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(JannahColors.Orange).padding(horizontal = JannahSpacing.gutter, vertical = 12.dp).windowInsetsPadding(WindowInsets.statusBars)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SURAH AL-MULK", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
                    Text(String.format("%02d:%02d", mm, ss), fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
            }
        }
    }
}
