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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DiaryClassifier
import com.jannahos.DiaryStore
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.RevealIn
import com.jannahos.ui.theme.CelebrationPulse
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(onBack: () -> Unit, onReacted: (String) -> Unit = {}) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val store = remember { DiaryStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = store.recent(30)

    var writing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var lastReaction by remember { mutableStateOf<DiaryClassifier.Result?>(null) }

    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

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
                Text("DIARY",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Tell it ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("anything.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Encrypted. Never uploaded. The app reacts.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (lastReaction != null && lastReaction!!.reactionTitle.isNotBlank()) {
                val r = lastReaction!!
                val body: @androidx.compose.runtime.Composable () -> Unit = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(
                            if (r.tag.startsWith("milestone")) JannahColors.Orange
                            else JannahColors.OrangeSoft
                        )
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text(r.reactionTitle,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (r.tag.startsWith("milestone")) JannahColors.OnOrange else JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(r.reactionBody,
                            style = JannahType.bodySmall,
                            color = if (r.tag.startsWith("milestone")) JannahColors.OnOrange.copy(alpha = 0.9f) else JannahColors.Body,
                            lineHeight = 20.sp)
                        if (r.source.isNotBlank()) {
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(r.source,
                                style = JannahType.micro,
                                color = if (r.tag.startsWith("milestone")) JannahColors.OnOrange.copy(alpha = 0.7f) else JannahColors.Faint)
                        }
                    }
                }
                }
                if (r.tag.startsWith("milestone")) {
                    RevealIn(delayMillis = 0) { CelebrationPulse { body() } }
                } else {
                    RevealIn(delayMillis = 0) { body() }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            if (!writing) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable { writing = true; lastReaction = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Write today",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    placeholder = {
                        Text("What did you do today? Say it plainly. It stays here.",
                            style = JannahType.bodySmall, color = JannahColors.Faint)
                    }
                )
                Spacer(Modifier.height(JannahSpacing.s))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(JannahColors.Card)
                            .clickable { writing = false; text = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = JannahType.button, color = JannahColors.Muted)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (text.isBlank()) JannahColors.Border else JannahColors.Orange)
                            .clickable {
                                if (text.isNotBlank()) {
                                    val result = DiaryClassifier.classify(text)
                                    val id = store.add(text.trim(), result.tag, result.reactionTitle)
                                    if (result.tag.startsWith("milestone")) {
                                        store.markCelebrated(id)
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    lastReaction = result
                                    text = ""
                                    writing = false
                                    refresh++
                                    onReacted(result.tag)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save",
                            style = JannahType.button,
                            color = if (text.isBlank()) JannahColors.Faint else JannahColors.OnOrange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            if (entries.isNotEmpty()) {
                Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                entries.forEachIndexed { i, e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        e.tag.startsWith("milestone") -> JannahColors.Orange
                                        e.tag.startsWith("struggle") -> JannahColors.Muted
                                        e.tag.startsWith("good") -> JannahColors.OrangeDeep
                                        else -> JannahColors.Border
                                    }
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dayFmt.format(Date(e.ts)),
                                style = JannahType.micro,
                                color = JannahColors.Faint)
                            Spacer(Modifier.height(4.dp))
                            Text(e.text,
                                style = JannahType.bodySmall,
                                color = JannahColors.Ink,
                                lineHeight = 20.sp)
                            if (!e.reaction.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("→ " + e.reaction,
                                    style = JannahType.caption,
                                    color = JannahColors.Muted,
                                    fontStyle = FontStyle.Italic)
                            }
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .clickable {
                                    store.delete(e.id)
                                    refresh++
                                }
                                .padding(6.dp)
                        ) {
                            Text("×", style = JannahType.body, color = JannahColors.Muted)
                        }
                    }
                    if (i < entries.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            } else {
                Text("Nothing written yet.",
                    style = JannahType.bodySmall,
                    color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
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
