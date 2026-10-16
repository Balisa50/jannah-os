package com.jannahos.ui.screens

import androidx.compose.foundation.background
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
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OfflineSetupScreen(onReady: () -> Unit) {
    val ctx = LocalContext.current
    var completed by remember { mutableIntStateOf(QuranStore.cachedCount(ctx)) }
    var finished by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) {
            QuranStore.downloadRange(ctx, 1, QuranStore.totalPages()) { done, _ ->
                completed = done
            }
        }
        if (ok) finished = true else failed = true
    }

    LaunchedEffect(finished) {
        if (finished) {
            kotlinx.coroutines.delay(400)
            onReady()
        }
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

        Text(
            "Downloading the Quran once. After this, it works without internet.",
            style = JannahType.body,
            color = JannahColors.Body,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(JannahSpacing.xxl))

        if (!failed) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(JannahColors.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(JannahColors.Orange)
                )
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "$completed of ${QuranStore.totalPages()} pages",
                style = JannahType.caption,
                color = JannahColors.Muted
            )
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "This takes a few minutes on mobile data. Leave the app open.",
                style = JannahType.caption,
                color = JannahColors.Faint,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                "Download interrupted. Reopen the app to continue where it left off.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}
