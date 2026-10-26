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

            Text("Jannah OS", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PURPOSE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "A utility for prayer focus and Quran reading. It uses locks, alarms, and a fixed reading plan to build habits. It is not a social network, a tracker, or a dashboard.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PRIVACY & DATA", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "All personal data (diary, khatm progress, bookmarks, settings) is stored locally on this device using SQLite and SharedPreferences. No accounts are required. No analytics are collected. Quran pages and tafsir are fetched from alquran.cloud via HTTPS based on page/surah numbers only.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("SOURCES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "Quran text: Uthmani Madani Mushaf. Hadith: Sahih al-Bukhari, Sahih Muslim, and others as cited. Calculation methods follow standard scholarly consensus (MWL, ISNA, etc.).",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
