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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.jannahos.CharacterLog
import com.jannahos.DailyHadith
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun CharacterScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    LaunchedEffect(Unit) { CharacterLog.prune(ctx) }

    val hadith = remember { DailyHadith.today() }
    val done = CharacterLog.doneToday(ctx)
    val total = CharacterLog.traits.size

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
                Text("TODAY'S HADITH",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("His words, ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("every day.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text(hadith.theme,
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Hadith card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text(hadith.arabic,
                        style = JannahType.arabicLarge,
                        color = JannahColors.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text(hadith.english,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        color = JannahColors.Body,
                        lineHeight = 26.sp)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text(hadith.source,
                        style = JannahType.micro,
                        color = JannahColors.Faint)
                    if (hadith.caveat.isNotBlank()) {
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(hadith.caveat,
                            style = JannahType.caption,
                            color = JannahColors.Muted,
                            lineHeight = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Character tracker header
            Text("CHARACTER CHECK · $done of $total",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.xs))
            Text("One tap per trait. Kept on your phone.",
                style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(JannahSpacing.m))

            CharacterLog.traits.forEachIndexed { i, t ->
                val isDone = CharacterLog.isDone(ctx, t.id)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            CharacterLog.toggle(ctx, t.id)
                            refresh = refresh + 1
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDone) JannahColors.Orange else JannahColors.Card)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (isDone) "✓" else "·",
                            style = JannahType.body,
                            color = if (isDone) JannahColors.OnOrange else JannahColors.Muted
                        )
                    }
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.label,
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(t.hint,
                            style = JannahType.caption,
                            color = JannahColors.Muted,
                            lineHeight = 16.sp)
                    }
                }
                if (i < CharacterLog.traits.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("WHY THIS",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The Prophet ﷺ said: \"I have only been sent to perfect good character.\" (Ahmad 8952, graded sahih by al-Albani). Character is not separate from worship, it is the fruit of it.",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
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
