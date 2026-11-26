package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val store = remember { runCatching { DiaryStore(ctx) }.getOrNull() }
    val storeAvailable = store != null
    
    var writing by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var text by remember { mutableStateOf("") }
    var saveError by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = remember(refresh) { runCatching { store?.recent(50, tag = "note") ?: emptyList() }.getOrDefault(emptyList()) }
    
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    if (writing) {
        Column(Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Cancel", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.clickable { writing = false; editingId = null; text = ""; saveError = null })
                Spacer(Modifier.weight(1f))
                Text("Save", style = JannahType.button, color = JannahColors.Orange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                    if (text.isNotBlank()) {
                        val result = runCatching {
                            val activeStore = store ?: error("Notes storage is unavailable.")
                            val id = editingId
                            if (id == null) check(activeStore.add(text.trim(), "note", "") > 0)
                            else check(activeStore.updateText(id, text.trim()))
                        }
                        if (result.isSuccess) {
                            editingId = null
                            text = ""
                            writing = false
                            saveError = null
                            refresh++
                        } else {
                            saveError = "Your note wasn't saved. Your text is still here; try again."
                        }
                    }
                })
            }
            if (saveError != null) {
                Text(saveError!!, style = JannahType.bodySmall, color = JannahColors.Danger, modifier = Modifier.padding(horizontal = 16.dp))
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it; saveError = null },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
                placeholder = { Text("Write your thoughts here...", color = JannahColors.Faint, fontSize = 18.sp) }
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
            ScreenHeader(title = "Notes", eyebrow = "PRIVATE", subtitle = "Write it down.", onBack = onBack)

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                    Spacer(Modifier.height(JannahSpacing.l))
                    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { editingId = null; text = ""; saveError = null; writing = true }, contentAlignment = Alignment.Center) { 
                        Text("New Note", style = JannahType.button, color = JannahColors.OnOrange) 
                    }
                    Spacer(Modifier.height(JannahSpacing.l))

                    if (saveError != null) {
                        Text(saveError!!, style = JannahType.bodySmall, color = JannahColors.Danger, modifier = Modifier.padding(vertical = JannahSpacing.s))
                    }
                    if (entries.isNotEmpty()) {
                        Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        entries.forEachIndexed { i, e ->
                            Column(Modifier.fillMaxWidth().clickable { 
                                text = e.text
                                editingId = e.id
                                saveError = null
                                writing = true 
                            }.padding(vertical = 12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(dayFmt.format(Date(e.ts)), style = JannahType.micro, color = JannahColors.Faint, modifier = Modifier.weight(1f))
                                    Box(Modifier.clip(RoundedCornerShape(50)).clickable {
                                        val deleted = runCatching { store?.delete(e.id) }
                                        if (deleted.isSuccess) refresh++ else saveError = "This note couldn't be deleted. Try again."
                                    }.padding(4.dp)) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(e.text.lines().take(2).joinToString("\n"), style = JannahType.body, color = JannahColors.Ink, lineHeight = 20.sp, maxLines = 2)
                            }
                            if (i < entries.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                        }
                    } else if (!storeAvailable) {
                        Text("Notes storage couldn't be opened. Your existing notes have not been changed.", style = JannahType.bodySmall, color = JannahColors.Danger, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                    } else {
                        Text("Nothing written yet.", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                    }
                    Spacer(Modifier.height(JannahSpacing.xxl))
                }
            }
        }
    }
}
