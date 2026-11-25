package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("About", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("SIRAJ", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PURPOSE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "An Android-first Islamic digital companion. SIRAJ is being rebuilt around trusted Islamic resources, personal tools and permissioned actions. Its first command interface can navigate the Quran, prayer times, Qibla, adhkar, notes and settings. Source-backed research and advanced AI assistance are still in development; until then, SIRAJ will not fabricate religious answers.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PRIVACY & DATA", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "SIRAJ does not require an account. Personal tools are designed to store their data on this device. Some Quran text and search features may contact their configured content provider. Voice input is handled by the Android speech-recognition service selected on your device; its network and privacy behaviour depends on that provider.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("SOURCES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "SIRAJ is strengthening source attribution for Quran translations, hadith collections, grading and scholarly references. The current legacy hadith dataset does not yet provide complete provenance and grading metadata, so it is not used as evidence for generated religious rulings.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
