package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
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
    val haptics = LocalHapticFeedback.current
    val store = remember { DiaryStore(ctx) }
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = store.recent(50)
    
    var writing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    val processText: (String) -> Unit = { newText ->
        var processedText = SmartTextEngine.process(newText)
        
        val quranRegex = Regex("/quran\\s+(\\d+):(\\d+)")
        quranRegex.find(processedText)?.let { match ->
            val (surah, ayah) = match.destructured
            val command = match.value
            isFetching = true
            scope.launch {
                val verse = IslamicContentApi.getVerse(surah.toInt(), ayah.toInt())
                processedText = processedText.replace(command, "\n\n${verse ?: "[Verse not found]"}\n\n")
                text = processedText
                isFetching = false
            }
        }

        val hadithRegex = Regex("/hadith\\s+(\\w+)\\s+(\\d+)")
        hadithRegex.find(processedText)?.let { match ->
            val (book, number) = match.destructured
            val command = match.value
            isFetching = true
            scope.launch {
                val hadith = IslamicContentApi.getHadith(book, number.toInt())
                processedText = processedText.replace(command, "\n\n${hadith ?: "[Hadith not found]"}\n\n")
                text = processedText
                isFetching = false
            }
        }
        
        text = processedText
    }

    if (writing) {
        // FULL SCREEN IMMERSIVE EDITOR
        Column(Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
            // Minimal Top Bar
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Cancel", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.clickable { writing = false; text = "" })
                Spacer(Modifier.weight(1f))
                if (isFetching) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = JannahColors.Orange, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save", style = JannahType.button, color = JannahColors.Orange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                    if (text.isNotBlank() && !isFetching) {
                        store.add(text.trim(), "note", "")
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        text = ""; writing = false; refresh++ 
                    }
                })
            }

            // The Canvas
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                BasicTextField(
                    value = text,
                    onValueChange = { processText(it) },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 19.sp,
                        color = JannahColors.Ink,
                        lineHeight = 30.sp,
                        textDirection = TextDirection.ContentOrLtr // Handles Arabic automatically
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxSize()
                )
                
                if (text.isEmpty()) {
                    Column {
                        Text(
                            "Start writing...",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = JannahColors.Faint,
                            lineHeight = 30.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Use / to insert Quran, Hadith, or Dua.\nType 'sallahu alaihi wa salaam' for ﷺ.",
                            style = JannahType.caption,
                            color = JannahColors.Orange.copy(alpha = 0.6f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Floating Help Button
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(JannahColors.OrangeSoft).clickable { showHelp = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", style = JannahType.body, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // LIST VIEW
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

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            containerColor = JannahColors.Surface,
            title = { Text("Smart Shortcuts", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = JannahColors.Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HelpItem("/quran [surah]:[ayah]", "Inserts the verse with translation.\nExample: /quran 2:255")
                    HelpItem("/hadith [book] [num]", "Inserts the hadith text.\nExample: /hadith bukhari 1")
                    HelpItem("sallahu alaihi wa salaam", "Auto-converts to ﷺ")
                    HelpItem("subhanahu wa taala", "Auto-converts to سبحانه وتعالى")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Got it", color = JannahColors.Orange, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun HelpItem(command: String, desc: String) {
    Column {
        Text(command, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
        Text(desc, style = JannahType.caption, color = JannahColors.Muted)
    }
}
