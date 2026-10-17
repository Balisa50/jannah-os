package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QuranStore
import com.jannahos.Settings
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OfflineSetupScreen(onReady: () -> Unit) {
    val ctx = LocalContext.current
    var completed by remember { mutableIntStateOf(QuranStore.cachedCount(ctx)) }
    var downloading by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(QuranStore.isFullyDownloaded(ctx)) }

    // If already complete on entry, skip immediately.
    LaunchedEffect(Unit) {
        if (QuranStore.isFullyDownloaded(ctx)) {
            Settings.setSetupComplete(ctx)
            onReady()
        }
    }

    // Only download if not already complete.
    LaunchedEffect(done) {
        if (done || downloading) return@LaunchedEffect
        downloading = true
        val start = QuranStore.cachedCount(ctx) + 1
        if (start > QuranStore.totalPages()) {
            done = true
            downloading = false
            return@LaunchedEffect
        }
        val ok = withContext(Dispatchers.IO) {
            QuranStore.downloadRange(ctx, start, QuranStore.totalPages()) { c, _ ->
                completed = c + (start - 1)
            }
        }
        downloading = false
        if (ok) done = true
    }

    val progress = (completed.toFloat() / QuranStore.totalPages()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Preparing.",
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.m))

        Text("$completed of ${QuranStore.totalPages()}",
            style = JannahType.bodySmall,
            color = JannahColors.Muted)

        Spacer(Modifier.height(JannahSpacing.xxl))

        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(JannahColors.Border)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(JannahColors.Orange)
            )
        }

        Spacer(Modifier.height(JannahSpacing.xxl))

        if (done) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        Settings.setSetupComplete(ctx)
                        onReady()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Continue",
                    style = JannahType.button,
                    color = JannahColors.OnOrange)
            }
        } else if (!downloading && completed > 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .clickable {
                        completed = QuranStore.cachedCount(ctx)
                        done = QuranStore.isFullyDownloaded(ctx)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Retry", style = JannahType.button, color = JannahColors.Ink)
            }
        }
    }
}
