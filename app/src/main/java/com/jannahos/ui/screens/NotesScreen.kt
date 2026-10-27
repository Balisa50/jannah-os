package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DiaryStore
import com.jannahos.IslamicContentApi
import com.jannahos.SmartTextEngine
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val store = remember { DiaryStore(ctx) }
    val scope = rememberCoroutineScope()
    
    var writing by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = store.recent(50)
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    if (writing) {
        NoteEditor(
            onCancel = { writing = false },
            onSave = { fullText ->
                if (fullText.isNotBlank()) {
                    store.add(fullText.trim(), "note", "")
                    writing = false
                    refresh++
                }
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
            ScreenHeader(title = "Notes", eyebrow = "PRIVATE", subtitle = "Write it down.", onBack = onBack)

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                    Spacer(Modifier.height(JannahSpacing.l))
                    
                    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { writing = true }, contentAlignment = Alignment.Center) { 
                        Text("New Note", style = JannahType.button, color = JannahColors.OnOrange) 
                    }
                    
                    Spacer(Modifier.height(JannahSpacing.l))

                    if (entries.isNotEmpty()) {
                        Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        entries.forEachIndexed { i, e ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.Top) {
                                Box(Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(JannahColors.Border))
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(dayFmt.format(Date(e.ts)), style = JannahType.micro, color = JannahColors.Faint)
                                    Spacer(Modifier.height(4.dp))
                                    Text(e.text, style = JannahType.body, color = JannahColors.Ink, lineHeight = 22.sp)
                                }
                                Box(Modifier.clip(RoundedCornerShape(JannahSpacing.pillRadius)).clickable { store.delete(e.id); refresh++ }.padding(6.dp)) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                            }
                            if (i < entries.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                        }
                    } else {
                        Text("Nothing written yet.", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                    }
                    Spacer(Modifier.height(JannahSpacing.xxl))
                }
            }
        }
    }
}

@Composable
fun NoteEditor(onCancel: () -> Unit, onSave: (String) -> Unit) {
    var currentText by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val processText: (String) -> Unit = { newText ->
        var processed = SmartTextEngine.process(newText)

        // 1. Quran Regex (Matches exactly 1 verse unless a range is specified)
        val quranRegex = Regex("/quran\\s+(\\d+):(\\d+)(?:-(\\d+))?")
        quranRegex.find(processed)?.let { match ->
            val surah = match.groupValues[1].toInt()
            val startAyah = match.groupValues[2].toInt()
            val endAyah = if (match.groupValues[3].isNotEmpty()) match.groupValues[3].toInt() else startAyah
            val command = match.value
            
            isFetching = true
            scope.launch {
                val res = IslamicContentApi.getVerseRange(surah, startAyah, endAyah)
                if (res != null) {
                    processed = processed.replace(command, "\n\n$res\n\n")
                }
                currentText = processed
                isFetching = false
            }
        }

        // 2. Hadith Regex
        val hadithRegex = Regex("/hadith\\s+(\\w+):(\\d+)")
        hadithRegex.find(processed)?.let { match ->
            val book = match.groupValues[1]
            val number = match.groupValues[2].toInt()
            val command = match.value
            
            isFetching = true
            scope.launch {
                val res = IslamicContentApi.getHadith(book, number)
                if (res != null) {
                    processed = processed.replace(command, "\n\n$res\n\n")
                }
                currentText = processed
                isFetching = false
            }
        }

        currentText = processed
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Cancel", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.clickable { onCancel() })
            Spacer(Modifier.weight(1f))
            if (isFetching) {
                Text("Fetching...", style = JannahType.caption, color = JannahColors.Orange, modifier = Modifier.padding(end = 12.dp))
            }
            Text("Save", style = JannahType.button, color = JannahColors.Orange, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.clickable {
                onSave(currentText)
            })
        }

        // Formatting Toolbar (Simple placeholders for now)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("**B**", style = JannahType.button, color = JannahColors.Ink, modifier = Modifier.background(JannahColors.Card).padding(8.dp))
            Text("_I_", style = JannahType.button, color = JannahColors.Ink, modifier = Modifier.background(JannahColors.Card).padding(8.dp))
            Text("Highlight", style = JannahType.caption, color = JannahColors.Ink, modifier = Modifier.background(JannahColors.OrangeSoft).padding(8.dp))
        }

        // The Single Text Field
        BasicTextField(
            value = currentText,
            onValueChange = { processText(it) },
            textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp).padding(top = 8.dp),
            decorationBox = { inner ->
                if (currentText.isEmpty()) {
                    Text("Start typing... Try /quran 2:45 or /hadith bukhari:1", color = JannahColors.Faint, fontSize = 18.sp)
                }
                inner()
            }
        )
    }
}
