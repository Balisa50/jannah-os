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
import androidx.compose.ui.text.font.FontStyle
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
    var progress by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) {
            QuranStore.downloadAll(ctx) { p -> progress = p }
        }
        if (ok) {
            done = true
        } else {
            error = "Could not download the Quran. Check your internet and try again."
        }
    }

    LaunchedEffect(done) {
        if (done) {
            kotlinx.coroutines.delay(500)
            onReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Setting up.",
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.m))

        Text(
            "Downloading the Quran once. After this, everything works without internet. Nothing is sent to a server.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            color = JannahColors.Orange,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(JannahSpacing.xxl))

        if (error == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(JannahColors.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(JannahColors.Orange)
                )
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                if (progress < 100) "$progress%" else "Done.",
                style = JannahType.caption,
                color = JannahColors.Muted
            )
        } else {
            Text(
                error!!,
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}
