package com.jannahos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Text("ABOUT",
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)

        Spacer(Modifier.height(JannahSpacing.s))

        Text("Why Jannah exists.",
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            lineHeight = 36.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        Text(
            "Most Islamic apps give you more to do. More notifications, more counters, more streaks, more dashboards. They treat worship as a productivity problem. They measure you, rank you, compare you to others.",
            style = JannahType.body,
            color = JannahColors.Body,
            lineHeight = 26.sp
        )

        Spacer(Modifier.height(JannahSpacing.m))

        Text(
            "This app is built on a different belief: that the deepest change comes from less, done well, over time.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            color = JannahColors.Orange,
            lineHeight = 28.sp
        )

        Spacer(Modifier.height(JannahSpacing.xl))

        Section(
            "WHAT WE WILL NEVER DO",
            listOf(
                "Ask you to create an account. Everything is on your phone.",
                "Send your data anywhere.",
                "Show you ads or sell anything. There is no business model that requires your attention.",
                "Track your behaviour across other apps.",
                "Send you a notification that isn't scheduled by you.",
                "Manufacture guilt to keep you opening the app.",
                "Promise that a particular number of nights creates a habit, or that a sin causes a specific hardship. Only Allah knows those things."
            )
        )

        Spacer(Modifier.height(JannahSpacing.l))

        Section(
            "WHAT WE WILL DO",
            listOf(
                "Cite the Quran by surah and verse. Every time.",
                "Cite hadith by collection and number, with authenticity grades where scholars differ.",
                "Never invent a reward, a virtue, or a Prophetic quote.",
                "Tell you when something is our own suggestion, not a religious claim.",
                "Let you leave. If the habit holds, we will tell you."
            )
        )

        Spacer(Modifier.height(JannahSpacing.l))

        Section(
            "YOUR DATA",
            listOf(
                "Everything is stored locally, on this phone.",
                "Databases are encrypted at rest with AES-256.",
                "You can export a backup at any time, as a file you control.",
                "You can delete everything with one button in Settings.",
                "There is no copy to give up."
            )
        )

        Spacer(Modifier.height(JannahSpacing.l))

        // The thesis
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
                    "An Islamic app that respects your time, your privacy, and your intelligence — and is built to become unnecessary.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Quranic closing
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

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            Modifier
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Ink)
                .clickable(onClick = onBack)
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
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
                        Text("—",
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
