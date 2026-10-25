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
import androidx.compose.ui.text.font.FontStyle
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        TopBar("About", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("Jannah OS", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("A quiet space for worship.", style = JannahType.body, color = JannahColors.Orange)

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PRIVACY", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "No accounts, no ads, no analytics. Your personal records — diary, deeds, khatm progress, bookmarks, private commitments — are stored only on this device and are never uploaded. Quran pages and tafsir are fetched on demand from alquran.cloud using only the page or surah number, not your identity. You can export or erase your records from Backup & restore at any time.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("SOURCES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "Quran verses are cited by Surah and Ayah. Hadith are cited by collection and number. Where scholars differ on authenticity, it is noted.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("PHILOSOPHY", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "Built to be used less. The goal is the habit, not the app.",
                style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Text(
                    "\"And say: My Lord, increase me in knowledge.\"",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = JannahColors.Ink,
                    lineHeight = 24.sp
                )
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
