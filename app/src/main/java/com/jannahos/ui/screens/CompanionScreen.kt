package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CompanionIndex
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun CompanionScreen(onBack: () -> Unit, onNavigate: (String) -> Unit = {}) {
    var input by remember { mutableStateOf("") }
    var need by remember { mutableStateOf<CompanionIndex.Need?>(null) }
    var searched by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("COMPANION",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Tell me ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("what's happening.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Type a feeling, a worry, a question. Sources answer.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What's on your mind?", fontSize = 12.sp) },
                placeholder = {
                    Text("e.g. \"I feel anxious\" / \"money is tight\" / \"I sinned\"",
                        fontSize = 13.sp)
                }
            )

            Spacer(Modifier.height(JannahSpacing.m))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(if (input.isBlank()) JannahColors.Border else JannahColors.Ink)
                    .clickable {
                        if (input.isNotBlank()) {
                            need = CompanionIndex.match(input)
                            searched = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Find me an answer",
                    style = JannahType.button,
                    color = if (input.isBlank()) JannahColors.Faint else JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.l))

            if (need != null) {
                val n = need!!
                Text(n.title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    lineHeight = 30.sp)

                Spacer(Modifier.height(JannahSpacing.l))

                // Verses
                n.verses.forEach { v ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.l)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(v.arabic,
                                style = JannahType.arabic,
                                color = JannahColors.Ink,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(v.english,
                                style = JannahType.body,
                                color = JannahColors.Body,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(v.source,
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }

                // Duas
                n.duas.forEach { d ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.l)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(d.arabic,
                                style = JannahType.arabic,
                                color = JannahColors.Ink,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(d.transliteration,
                                style = JannahType.caption,
                                color = JannahColors.Muted,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(d.english,
                                style = JannahType.bodySmall,
                                color = JannahColors.Body,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(d.source,
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }

                // Hadith
                n.ahadith.forEach { h ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.OrangeSoft)
                            .padding(JannahSpacing.l)
                    ) {
                        Column {
                            Text(h.english,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                                color = JannahColors.Ink,
                                lineHeight = 24.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(h.source,
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }

                if (n.actionLabel != null && n.actionRoute != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Orange)
                            .clickable {
                                onNavigate(n.actionRoute)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(n.actionLabel,
                            style = JannahType.button, color = JannahColors.OnOrange)
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                }
            } else if (searched) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("I don't have a match for that.",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("Try a different word. Or open the dua library and search there. Everything is sourced.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                // Show what the companion can talk about
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("It knows about:",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        CompanionIndex.needs.forEach { n ->
                            val label = n.id.replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                            Text("· $label",
                                style = JannahType.bodySmall,
                                color = JannahColors.Body)
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text("On-device only. No cloud. No AI service reading your words.",
                            style = JannahType.micro, color = JannahColors.Faint)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

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
}
