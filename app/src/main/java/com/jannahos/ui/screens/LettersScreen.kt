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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.LetterStore
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LettersScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { LetterStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val ready = store.ready()
    val sealed = store.sealed()

    var composing by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf<LetterStore.Letter?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var daysAhead by remember { mutableStateOf(30) }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    if (reading != null) {
        ReadingScreen(reading!!, onClose = {
            store.markOpened(reading!!.id)
            reading = null
            refresh++
        })
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
                Text("LETTERS",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("To your ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("future self.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Write it. Seal it. It opens on the day you choose.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Letters that have arrived
            if (ready.isNotEmpty()) {
                Text("ARRIVED",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                ready.forEach { l ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.OrangeDeep)
                            .clickable { reading = l }
                            .padding(JannahSpacing.l)
                    ) {
                        Column {
                            Text("A LETTER FROM THE PAST",
                                style = JannahType.eyebrow,
                                color = JannahColors.OnOrange.copy(alpha = 0.7f))
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(l.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.OnOrange,
                                lineHeight = 26.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text("Written ${fmt.format(Date(l.created))}",
                                style = JannahType.caption,
                                color = JannahColors.OnOrange.copy(alpha = 0.75f))
                            Spacer(Modifier.height(JannahSpacing.m))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                    .background(JannahColors.OnOrange.copy(alpha = 0.18f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Open the letter",
                                    style = JannahType.caption,
                                    color = JannahColors.OnOrange,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Compose
            if (!composing) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable { composing = true; savedMsg = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Write a letter",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }

                if (savedMsg != null) {
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(savedMsg!!,
                        style = JannahType.caption, color = JannahColors.Orange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                Text("WRITE",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Give it a title", fontSize = 11.sp) },
                    placeholder = { Text("e.g. \"A note before Ramadan\"", fontSize = 13.sp) }
                )
                Spacer(Modifier.height(JannahSpacing.s))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    label = { Text("Your letter", fontSize = 11.sp) },
                    placeholder = {
                        Text("What do you want to say to the version of you who reads this? " +
                            "What are you hoping for? What are you afraid of? Write it plainly.",
                            fontSize = 13.sp)
                    }
                )
                Spacer(Modifier.height(JannahSpacing.m))

                Text("OPEN IN",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                ) {
                    listOf(30, 60, 90).forEach { d ->
                        val selected = daysAhead == d
                        Box(
                            Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(if (selected) JannahColors.Orange else JannahColors.Card)
                                .clickable { daysAhead = d },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$d days",
                                style = JannahType.button,
                                color = if (selected) JannahColors.OnOrange else JannahColors.Ink)
                        }
                    }
                }

                Spacer(Modifier.height(JannahSpacing.m))

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
                            .clickable { composing = false; title = ""; body = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = JannahType.button, color = JannahColors.Muted)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(
                                if (title.isBlank() || body.isBlank()) JannahColors.Border
                                else JannahColors.Orange
                            )
                            .clickable {
                                if (title.isNotBlank() && body.isNotBlank()) {
                                    store.add(title, body, daysAhead)
                                    title = ""
                                    body = ""
                                    composing = false
                                    savedMsg = "Sealed. It opens in $daysAhead days."
                                    refresh++
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Seal",
                            style = JannahType.button,
                            color = if (title.isBlank() || body.isBlank()) JannahColors.Faint
                            else JannahColors.OnOrange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Sealed letters
            if (sealed.isNotEmpty()) {
                Text("SEALED",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                sealed.forEachIndexed { i, l ->
                    val daysLeft = ((l.opensAt - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).toInt() + 1
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(l.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text("Opens ${fmt.format(Date(l.opensAt))} · in $daysLeft day${if (daysLeft == 1) "" else "s"}",
                                style = JannahType.caption, color = JannahColors.Muted)
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { store.delete(l.id); refresh++ }
                                .padding(6.dp)
                        ) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                    }
                    if (i < sealed.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            } else if (savedMsg == null && !composing) {
                Spacer(Modifier.height(JannahSpacing.l))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("Why this?",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "The person you are in 90 days is not the person you are today. Write to them. Say what you hope for, what you fear. When the day arrives, you will read it and recognise yourself — or realise how far you have come.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp
                        )
                    }
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
private fun ReadingScreen(letter: LetterStore.Letter, onClose: () -> Unit) {
    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Text("A LETTER FROM",
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text(fmt.format(Date(letter.created)),
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)

        Spacer(Modifier.height(JannahSpacing.l))

        Text(letter.title,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 20.sp,
            color = JannahColors.Orange,
            lineHeight = 28.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Text(letter.body,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                color = JannahColors.Body,
                lineHeight = 26.sp)
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Ink)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("Close", style = JannahType.button, color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
