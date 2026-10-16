package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

            Text("Why this exists.",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                lineHeight = 34.sp)

            Spacer(Modifier.height(JannahSpacing.l))

            Text(
                "Most Islamic apps give you more to do. More notifications, more counters, more streaks. They measure you. This app was built on the opposite idea: that the deepest change comes from less, done well, over time.",
                style = JannahType.body,
                color = JannahColors.Body,
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Section(
                "WHAT WE WILL NEVER DO",
                listOf(
                    "Ask you to create an account.",
                    "Send your data anywhere.",
                    "Show ads or sell anything.",
                    "Track your behaviour across other apps.",
                    "Send a notification you did not schedule."
                )
            )

            Spacer(Modifier.height(JannahSpacing.l))

            Section(
                "WHAT WE WILL DO",
                listOf(
                    "Cite the Quran by surah and verse.",
                    "Cite hadith by collection and number, with authenticity grades where scholars differ.",
                    "Tell you when something is our suggestion, not a religious claim.",
                    "Let you leave. If the habit holds without the app, we will say so."
                )
            )

            Spacer(Modifier.height(JannahSpacing.l))

            Section(
                "YOUR DATA",
                listOf(
                    "Everything is stored on this phone.",
                    "Databases are encrypted at rest with AES-256.",
                    "You can export a backup at any time, as a file you control.",
                    "You can delete everything with one button in Settings.",
                    "There is no copy of your data anywhere else."
                )
            )

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("THE ONE-LINE THESIS",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "An Islamic app that respects your time, your privacy, and your intelligence, and is built to become unnecessary.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text(
                        "\"And say: My Lord, increase me in knowledge.\"",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        color = JannahColors.Body,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("Qur'an 20:114",
                        style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun Section(title: String, items: List<String>) {
    Column(Modifier.fillMaxWidth()) {
        Text(title,
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(JannahSpacing.s))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Column {
                items.forEachIndexed { i, item ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("·",
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            color = JannahColors.Orange,
                            modifier = Modifier.padding(end = JannahSpacing.s))
                        Text(item,
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp)
                    }
                    if (i < items.size - 1) Spacer(Modifier.height(JannahSpacing.s))
                }
            }
        }
    }
}
