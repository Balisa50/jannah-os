package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun SoundProfileScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(
            title = "Sound profiles",
            eyebrow = "AUDIO",
            subtitle = "Audio playback is not active in this build.",
            onBack = onBack
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            Spacer(Modifier.height(JannahSpacing.l))
            Text(
                "Prayer alerts currently use Android notifications and vibration. The named Adhan recordings are not wired into playback yet, so choosing a profile here would not change what you hear.",
                style = JannahType.body,
                color = JannahColors.Body,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(JannahSpacing.l))
            Text(
                "We will add this when SIRAJ has properly licensed offline audio, playback controls and a reliable download flow. Until then, this screen will not pretend a selection has been applied.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
