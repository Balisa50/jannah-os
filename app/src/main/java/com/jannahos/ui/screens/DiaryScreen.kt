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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DiaryClassifier
import com.jannahos.DiaryStore
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val store = remember { DiaryStore(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entries = store.recent(30)
    var writing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Diary", eyebrow = "PRIVATE", subtitle = "Tell it anything.", onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Spacer(Modifier.height(JannahSpacing.l))
                
                if (!writing) {
                    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { writing = true }, contentAlignment = Alignment.Center) { 
                        Text("Write today", style = JannahType.button, color = JannahColors.OnOrange) 
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                } else {
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        placeholder = { Text("What did you do today? Say it plainly. It stays here.", style = JannahType.bodySmall, color = JannahColors.Faint) }
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)) {
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card).clickable { writing = false; text = "" }, contentAlignment = Alignment.Center) { Text("Cancel", style = JannahType.button, color = JannahColors.Muted) }
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(if (text.isBlank()) JannahColors.Border else JannahColors.Orange).clickable {
                            if (text.isNotBlank()) {
                                val result = DiaryClassifier.classify(text)
                                store.add(text.trim(), result.tag, result.reactionTitle)
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                text = ""; writing = false; refresh++ 
                            }
                        }, contentAlignment = Alignment.Center) { Text("Save", style = JannahType.button, color = if (text.isBlank()) JannahColors.Faint else JannahColors.OnOrange) }
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                }

                if (entries.isNotEmpty()) {
                    Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    entries.forEachIndexed { i, e ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(when { e.tag.startsWith("milestone") -> JannahColors.Orange; e.tag.startsWith("struggle") -> JannahColors.Muted; else -> JannahColors.Border }))
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
                    Text("Nothing written yet.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                }
                Spacer(Modifier.height(JannahSpacing.xxl))
            }
        }
    }
}
