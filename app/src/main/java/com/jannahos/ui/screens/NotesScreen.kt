package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DiaryStore
import com.jannahos.IslamicContentApi
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
    var text by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = store.recent(50)
    
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    val processText: (String) -> Unit = { newText ->
        text = newText
        
        // Regex that supports single verses AND ranges (e.g., 2:255 or 2:255-257)
        val quranRegex = Regex("/quran\\s+(\\d+):(\\d+)(?:-(\\d+))?")
        quranRegex.find(text)?.let { match ->
            val surah = match.groupValues[1].toIntOrNull()
            val startAyah = match.groupValues[2].toIntOrNull()
            val endAyah = if (match.groupValues[3].isNotEmpty()) match.groupValues[3].toIntOrNull() else startAyah
            
            if (surah != null && startAyah != null && endAyah != null) {
                val command = match.value
                isFetching = true
                scope.launch {
                    val res = IslamicContentApi.getVerseRange(surah, startAyah, endAyah)
                    if (res != null) {
                        text = text.replace(command, "\n\n$res\n\n")
                    } else {
                        text = text.replace(command, "\n\n[Verse not found]\n\n")
                    }
                    isFetching = false
                }
            }
        }
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
                        text = ""; writing = false; refresh++ 
                    }
                })
            }

            OutlinedTextField(
                value = text,
                onValueChange = { processText(it) },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
                placeholder = { Text("Type /quran 2:255 or /quran 2:255-257...", color = JannahColors.Faint, fontSize = 18.sp) }
            )
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
                            Column(Modifier.fillMaxWidth().clickable { 
                                text = e.text
                                writing = true 
                            }.padding(vertical = 12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(dayFmt.format(Date(e.ts)), style = JannahType.micro, color = JannahColors.Faint, modifier = Modifier.weight(1f))
                                    Box(Modifier.clip(RoundedCornerShape(50)).clickable { store.delete(e.id); refresh++ }.padding(4.dp)) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(e.text.lines().take(2).joinToString("\n"), style = JannahType.body, color = JannahColors.Ink, lineHeight = 20.sp, maxLines = 2)
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
