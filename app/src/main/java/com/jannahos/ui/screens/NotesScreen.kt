package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    // Function to process the text field
    val processText: (String) -> Unit = { newText ->
        var processedText = SmartTextEngine.process(newText)
        
        // 1. Check for Quran
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

        // 2. Check for Hadith
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

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Notes", eyebrow = "PRIVATE", subtitle = "Write it down.", onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Spacer(Modifier.height(JannahSpacing.l))
                
                if (!writing) {
                    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { writing = true }, contentAlignment = Alignment.Center) { 
                        Text("New Note", style = JannahType.button, color = JannahColors.OnOrange) 
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("TYPE / FOR SHORTCUTS", style = JannahType.caption, color = JannahColors.OrangeDeep, modifier = Modifier.weight(1f))
                        Box(Modifier.size(24.dp).clip(CircleShape).background(JannahColors.OrangeSoft).clickable { showHelp = true }, contentAlignment = Alignment.Center) {
                            Text("?", style = JannahType.body, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = text,
                        onValueChange = { processText(it) },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
                        placeholder = { Text("Try /quran 2:23 or /hadith bukhari 1", style = JannahType.bodySmall, color = JannahColors.Faint) }
                    )
                    
                    if (isFetching) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = JannahColors.Orange, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Fetching...", style = JannahType.caption, color = JannahColors.Orange)
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)) {
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card).clickable { writing = false; text = "" }, contentAlignment = Alignment.Center) { Text("Cancel", style = JannahType.button, color = JannahColors.Muted) }
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(if (text.isBlank() || isFetching) JannahColors.Border else JannahColors.Orange).clickable {
                            if (text.isNotBlank() && !isFetching) {
                                store.add(text.trim(), "note", "")
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                text = ""; writing = false; refresh++ 
                            }
                        }, contentAlignment = Alignment.Center) { Text("Save", style = JannahType.button, color = if (text.isBlank() || isFetching) JannahColors.Faint else JannahColors.OnOrange) }
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                }

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

    // SOPHISTICATED SETTINGS / HELP DIALOG
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
