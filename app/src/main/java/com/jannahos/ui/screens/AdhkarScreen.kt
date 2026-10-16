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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.MorningEveningAdhkar
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun AdhkarScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    var morning by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val items = if (morning) MorningEveningAdhkar.morning else MorningEveningAdhkar.evening

    if (selectedIndex != null) {
        AdhkarDetail(
            item = items[selectedIndex!!],
            onBack = { selectedIndex = null }
        )
        return
    }

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
                Text("ADHKAR",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text(if (morning) "Morning " else "Evening ",
                        style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                    Text("remembrance.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (morning) "Between Fajr and sunrise."
                    else "Between Asr and Maghrib.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f)
                )
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Morning / Evening toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(if (morning) JannahColors.Orange else JannahColors.Card)
                        .clickable { morning = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Morning",
                        style = JannahType.button,
                        color = if (morning) JannahColors.OnOrange else JannahColors.Muted)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(if (!morning) JannahColors.Orange else JannahColors.Card)
                        .clickable { morning = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Evening",
                        style = JannahType.button,
                        color = if (!morning) JannahColors.OnOrange else JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            items.forEachIndexed { i, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedIndex = i }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.translation.take(60) + if (item.translation.length > 60) "…" else "",
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("× ${item.count}",
                            style = JannahType.caption,
                            color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < items.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

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

@Composable
private fun AdhkarDetail(item: MorningEveningAdhkar.Item, onBack: () -> Unit) {
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        
        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(item.arabic,
                    style = JannahType.arabicLarge,
                    color = JannahColors.Ink,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(item.transliteration,
                    style = JannahType.bodySmall,
                    color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(item.translation,
                    style = JannahType.body,
                    color = JannahColors.Body,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(item.source,
                    style = JannahType.micro,
                    color = JannahColors.Faint,
                    textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Simple count row
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Orange)
                .clickable { if (count < item.count) count++ },
            contentAlignment = Alignment.Center
        ) {
            Text("$count / ${item.count}  ·  tap to count",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .clickable { count = 0 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Reset count", style = JannahType.caption, color = JannahColors.Muted)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
