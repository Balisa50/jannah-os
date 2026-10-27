package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DiaryStore
import com.jannahos.IslamicContentApi
import com.jannahos.SmartTextEngine
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class NoteBlock {
    data class Text(val content: String) : NoteBlock()
    data class Quran(val surah: Int, val ayah: Int, val arabic: String, val english: String) : NoteBlock()
    data class Hadith(val book: String, val number: Int, val text: String) : NoteBlock()
}

@Composable
fun NotesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val store = remember { DiaryStore(ctx) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var blocks by remember { mutableStateOf(listOf<NoteBlock>()) }
    var currentText by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    var showSlashMenu by remember { mutableStateOf(false) }

    val processCommand = { input: String ->
        if (input.trim() == "/quran 2:255") {
            isFetching = true
            showSlashMenu = false
            scope.launch {
                val res = IslamicContentApi.getVerse(2, 255)
                if (res != null) {
                    val parts = res.split("\n\n")
                    blocks = blocks + NoteBlock.Quran(2, 255, parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
                }
                currentText = ""
                isFetching = false
            }
        } else if (input.startsWith("/hadith bukhari:1")) {
            isFetching = true
            showSlashMenu = false
            scope.launch {
                val res = IslamicContentApi.getHadith("bukhari", 1)
                if (res != null) {
                    blocks = blocks + NoteBlock.Hadith("Bukhari", 1, res)
                }
                currentText = ""
                isFetching = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Save", style = JannahType.button, color = JannahColors.Orange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                if (blocks.isNotEmpty() || currentText.isNotBlank()) {
                    val fullText = blocks.joinToString("\n\n") { 
                        when(it) {
                            is NoteBlock.Text -> it.content
                            is NoteBlock.Quran -> "${it.arabic}\n\"${it.english}\"\n[Surah ${it.surah}:${it.ayah}]"
                            is NoteBlock.Hadith -> "${it.text}\n[Sahih ${it.book}:${it.number}]"
                        }
                    } + "\n\n" + currentText
                    store.add(fullText.trim(), "note", "")
                    onBack()
                }
            })
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            itemsIndexed(blocks) { index, block ->
                when (block) {
                    is NoteBlock.Text -> Text(block.content, fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp, modifier = Modifier.padding(bottom = 16.dp))
                    is NoteBlock.Quran -> {
                        // DISTINCT LAYER: Quran Block
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                            Text(block.arabic, fontFamily = FontFamily.Serif, fontSize = 26.sp, color = JannahColors.Ink, textAlign = androidx.compose.ui.text.style.TextAlign.Right, modifier = Modifier.fillMaxWidth(), textDirection = TextDirection.Rtl)
                            Spacer(Modifier.height(12.dp))
                            Text(block.english, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontStyle = FontStyle.Italic, color = JannahColors.Body, lineHeight = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("— Surah ${block.surah}:${block.ayah}", style = JannahType.micro, color = JannahColors.OrangeDeep)
                        }
                    }
                    is NoteBlock.Hadith -> {
                        // DISTINCT LAYER: Hadith Block
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(RoundedCornerShape(12.dp)).background(JannahColors.Card).padding(16.dp)) {
                            Text(block.text, fontFamily = FontFamily.SansSerif, fontSize = 16.sp, color = JannahColors.Ink, lineHeight = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("— Sahih ${block.book}, Hadith ${block.number}", style = JannahType.micro, color = JannahColors.Muted)
                        }
                    }
                }
            }
            
            item {
                // ACTIVE TYPING FIELD
                BasicTextField(
                    value = currentText,
                    onValueChange = {
                        currentText = SmartTextEngine.process(it)
                        showSlashMenu = currentText.endsWith("/")
                        if (currentText.endsWith(" ")) processCommand(currentText)
                    },
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    decorationBox = { inner ->
                        if (currentText.isEmpty()) {
                            Text("Start typing... Use / for commands.", color = JannahColors.Faint, fontSize = 18.sp)
                        }
                        inner()
                    }
                )
            }
        }
        
        // Slash Menu
        if (showSlashMenu) {
            Row(Modifier.fillMaxWidth().background(JannahColors.Surface).padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Text("/quran 2:255", style = JannahType.caption, color = JannahColors.Orange, modifier = Modifier.clickable { 
                    currentText = "/quran 2:255"
                    processCommand(currentText)
                })
                Text("/hadith bukhari:1", style = JannahType.caption, color = JannahColors.Orange, modifier = Modifier.clickable { 
                    currentText = "/hadith bukhari:1"
                    processCommand(currentText)
                })
            }
        }
    }
}
