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
import com.jannahos.DuaJournalStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DuaJournalScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    val store = remember { DuaJournalStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val duas = store.all()
    val answered = store.answeredCount()
    val pending = store.pendingCount()

    var composing by remember { mutableStateOf(false) }
    var ask by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("rizq") }
    var answering by remember { mutableStateOf<DuaJournalStore.Dua?>(null) }
    var answerNote by remember { mutableStateOf("") }

    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    // Answer modal
    if (answering != null) {
        val d = answering!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(JannahColors.Cream)
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("ALHAMDULILLAH", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("Allah answered.",
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                lineHeight = 36.sp)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("You asked for:",
                style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(JannahSpacing.xs))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Text(d.ask,
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = JannahColors.Body,
                    lineHeight = 24.sp)
            }
            Spacer(Modifier.height(JannahSpacing.l))
            OutlinedTextField(
                value = answerNote,
                onValueChange = { answerNote = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                label = { Text("How did it come? (optional)", fontSize = 11.sp) }
            )
            Spacer(Modifier.height(JannahSpacing.l))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Card)
                        .clickable { answering = null; answerNote = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Later", style = JannahType.button, color = JannahColors.Muted)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Orange)
                        .clickable {
                            store.markAnswered(d.id, answerNote.ifBlank { null })
                            answering = null
                            answerNote = ""
                            refresh++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Record it", style = JannahType.button, color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
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
                Text("DUA JOURNAL",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("$answered ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("answered.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("$pending still waiting on Allah.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Compose or the encouragement
            if (!composing) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .clickable { composing = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Name a dua you are making",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                Text("WHAT ARE YOU ASKING FOR?",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("Be specific. \"Rizq\" is vague. \"A job that lets me pray on time\" is a record.",
                    style = JannahType.caption, color = JannahColors.Muted,
                    lineHeight = 18.sp)
                Spacer(Modifier.height(JannahSpacing.m))

                OutlinedTextField(
                    value = ask,
                    onValueChange = { ask = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    label = { Text("Your dua", fontSize = 11.sp) }
                )
                Spacer(Modifier.height(JannahSpacing.m))

                Text("CATEGORY",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DuaJournalStore.CATEGORIES.chunked(2).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            row.forEach { (code, label) ->
                                val sel = category == code
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sel) JannahColors.OrangeSoft else JannahColors.Card)
                                        .clickable { category = code }
                                        .padding(horizontal = 10.dp, vertical = 12.dp)
                                ) {
                                    Text(label,
                                        style = JannahType.caption,
                                        color = if (sel) JannahColors.OrangeDeep else JannahColors.Body,
                                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
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
                            .clickable { composing = false; ask = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = JannahType.button, color = JannahColors.Muted)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(if (ask.isBlank()) JannahColors.Border else JannahColors.Orange)
                            .clickable {
                                if (ask.isNotBlank()) {
                                    store.add(category, ask.trim())
                                    ask = ""
                                    composing = false
                                    refresh++
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Record",
                            style = JannahType.button,
                            color = if (ask.isBlank()) JannahColors.Faint else JannahColors.OnOrange)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // List, answered first
            if (duas.isNotEmpty()) {
                Text("YOUR RECORD", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                duas.forEachIndexed { i, d ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(if (d.answered) JannahColors.Card else JannahColors.OrangeSoft)
                            .padding(JannahSpacing.l)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (d.answered) "ANSWERED"
                                        else "WAITING",
                                        style = JannahType.micro,
                                        color = if (d.answered) JannahColors.OrangeDeep else JannahColors.Muted
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(DuaJournalStore.label(d.category),
                                        style = JannahType.caption,
                                        color = JannahColors.Faint)
                                }
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { store.delete(d.id); refresh++ }
                                        .padding(6.dp)
                                ) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                            }
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(d.ask,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                color = JannahColors.Ink,
                                lineHeight = 24.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            if (d.answered && d.answeredAt != null) {
                                Text("Answered ${fmt.format(Date(d.answeredAt))}",
                                    style = JannahType.caption,
                                    color = JannahColors.Orange,
                                    fontWeight = FontWeight.SemiBold)
                                if (!d.answerNote.isNullOrBlank()) {
                                    Spacer(Modifier.height(JannahSpacing.s))
                                    Text(d.answerNote,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        color = JannahColors.Body,
                                        lineHeight = 20.sp)
                                }
                            } else {
                                Text("Asked ${fmt.format(Date(d.created))}",
                                    style = JannahType.caption, color = JannahColors.Muted)
                            }
                            Spacer(Modifier.height(JannahSpacing.m))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                            ) {
                                if (!d.answered) {
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                            .background(JannahColors.Orange)
                                            .clickable {
                                                answering = d
                                                answerNote = ""
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Allah answered it",
                                            style = JannahType.button,
                                            color = JannahColors.OnOrange)
                                    }
                                } else {
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                            .background(JannahColors.Cream)
                                            .clickable {
                                                store.unmarkAnswered(d.id)
                                                refresh++
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Unmark",
                                            style = JannahType.caption,
                                            color = JannahColors.Muted)
                                    }
                                }
                            }
                        }
                    }
                    if (i < duas.size - 1) Spacer(Modifier.height(JannahSpacing.s))
                }
            } else if (!composing) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("Why this matters",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "The Prophet ﷺ said: \"Whoever does not ask Allah, He becomes angry with him.\" (Jami' at-Tirmidhi 3373). When hardship comes, we forget every dua Allah has already answered. This is your honest record. When you feel forgotten, come back and read it.",
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
