package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    var showSlashMenu by remember { mutableStateOf(false) }
    
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    val processText: (String) -> Unit = { newText ->
        var processedText = SmartTextEngine.process(newText)
        
        // Show menu if user just typed "/" or a command starting with "/"
        showSlashMenu = processedText.endsWith("/") || (processedText.contains("/") && processedText.lines().last().startsWith("/") && !processedText.lines().last().contains(" "))

        // Parse /quran (supports 2:255 or 2:255-257)
        val quranRegex = Regex("/quran\\s+(\\d+):(\\d+)(?:-(\\d+))?")
        quranRegex.find(processedText)?.let { match ->
            val surah = match.groupValues[1].toInt()
            val startAyah = match.groupValues[2].toInt()
            val endAyah = if (match.groupValues[3].isNotEmpty()) match.groupValues[3].toInt() else startAyah
            val command = match.value
            
            isFetching = true
            scope.launch {
                val verse = IslamicContentApi.getVerseRange(surah, startAyah, endAyah)
                processedText = processedText.replace(command, "\n\n${verse ?: "[Verse not found]"}\n\n")
                text = processedText
                isFetching = false
            }
        }

        // Parse /hadith
        val hadithRegex = Regex("/hadith\\s+(\\w+):(\\d+)")
        hadithRegex.find(processedText)?.let { match ->
            val book = match.groupValues[1]
            val number = match.groupValues[2].toInt()
            val command = match.value
            
            isFetching = true
            scope.launch {
                val hadith = IslamicContentApi.getHadith(book, number)
                processedText = processedText.replace(command, "\n\n${hadith ?: "[Hadith not found]"}\n\n")
                text = processedText
                isFetching = false
            }
        }

        // Parse /arabic
        val arabicRegex = Regex("/arabic\\s+")
        if (processedText.contains(arabicRegex)) {
            processedText = processedText.replace(arabicRegex, "\n\n") // Just clears the command, user types Arabic
        }
        
        text = processedText
    }

    if (writing) {
        Column(Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
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

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                TextField(
                    value = text,
                    onValueChange = { processText(it) },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 19.sp,
                        color = JannahColors.Ink,
                        lineHeight = 30.sp,
                        textDirection = TextDirection.ContentOrLtr
                    ),
                    placeholder = { 
                        Column {
                            Text("Start writing...", fontFamily = FontFamily.Serif, fontSize = 24.sp, color = JannahColors.Faint, lineHeight = 30.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Use / to insert Quran, Hadith, or Arabic.\nType 'pbuh' for ﷺ.", style = JannahType.caption, color = JannahColors.Orange.copy(alpha = 0.6f), lineHeight = 20.sp)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // SLASH MENU PALETTE
            AnimatedVisibility(
                visible = showSlashMenu,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                Column(Modifier.fillMaxWidth().background(JannahColors.Surface).padding(8.dp)) {
                    Text("COMMANDS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
                    
                    val commands = listOf(
                        "/quran 2:255" to "Insert Verse",
                        "/quran 2:255-257" to "Insert Verse Range",
                        "/hadith bukhari:1" to "Insert Hadith",
                        "/arabic " to "Start Arabic Block",
                        "pbuh " to "Insert ﷺ"
                    )
                    
                    commands.forEach { (cmd, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                // Replace the currently typed "/..." with the selected command
                                val currentLines = text.lines().toMutableList()
                                if (currentLines.isNotEmpty() && currentLines.last().startsWith("/")) {
                                    currentLines[currentLines.size - 1] = cmd
                                    text = currentLines.joinToString("\n")
                                }
                                showSlashMenu = false
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cmd.split(" ")[0], style = JannahType.micro, color = JannahColors.Orange, modifier = Modifier.width(80.dp))
                            Text(label, style = JannahType.bodySmall, color = JannahColors.Ink)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(JannahColors.OrangeSoft).clickable { showHelp = true }, contentAlignment = Alignment.Center) {
                    Text("?", style = JannahType.body, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
                }
            }
        }
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

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            containerColor = JannahColors.Surface,
            title = { Text("Smart Dictionary", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = JannahColors.Ink) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    HelpItem("/quran 2:255", "Inserts a single verse with translation.")
                    HelpItem("/quran 2:255-257", "Inserts a range of verses.")
                    HelpItem("/hadith bukhari:1", "Inserts a hadith (use : instead of space).")
                    HelpItem("/arabic", "Starts an Arabic typing block.")
                    Divider(color = JannahColors.Divider)
                    HelpItem("pbuh / saw", "Auto-converts to صلى الله عليه وسلم")
                    HelpItem("swt", "Auto-converts to سبحانه وتعالى")
                    HelpItem("ra", "Auto-converts to رضي الله عنه")
                    HelpItem("insh", "Auto-converts to إن شاء الله")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Close", color = JannahColors.Orange, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun HelpItem(command: String, desc: String) {
    Column {
        Text(command, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
        Text(desc, style = JannahType.caption, color = JannahColors.Muted, lineHeight = 18.sp)
    }
}
