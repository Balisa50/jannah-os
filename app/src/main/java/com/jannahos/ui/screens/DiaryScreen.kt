package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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

private data class SlashCommand(val trigger: String, val label: String, val content: String)

private val COMMANDS = listOf(
    SlashCommand(
        "/fatihah", "Surah Al-Fatihah",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾\n\nThe Opening."
    ),
    SlashCommand(
        "/kursi", "Ayat al-Kursi",
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ\n\nAllah - there is no deity except Him, the Ever-Living, the Sustainer of existence. (2:255)"
    ),
    SlashCommand(
        "/anxiety", "Dua for Anxiety",
        "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ\n\nO Allah, I seek refuge in You from anxiety and sorrow."
    ),
    SlashCommand(
        "/forgive", "Sayyid al-Istighfar",
        "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ\n\nO Allah, You are my Lord, none has the right to be worshipped but You. You created me and I am Your servant."
    ),
    SlashCommand(
        "/hadith", "Hadith on Intentions",
        "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ\n\nActions are judged by intentions. (Bukhari 1)"
    )
)

@Composable
fun DiaryScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val store = remember { runCatching { DiaryStore(ctx) }.getOrNull() }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val entriesResult = remember(refresh) { runCatching { store?.recent(30, excludeTag = "note") ?: emptyList() } }
    val entries = entriesResult.getOrDefault(emptyList())
    val storeAvailable = store != null && entriesResult.isSuccess
    var writing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showSlashMenu by remember { mutableStateOf(false) }
    val dayFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Diary", eyebrow = "PRIVATE", subtitle = "Tell it anything.", onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Spacer(Modifier.height(JannahSpacing.l))
                
                if (!writing) {
                    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { saveError = null; writing = true }, contentAlignment = Alignment.Center) { 
                        Text("Write today", style = JannahType.button, color = JannahColors.OnOrange) 
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                } else {
                    // THE ELITE EDITOR
                    Box {
                        androidx.compose.foundation.text.BasicTextField(
                            value = text,
                            onValueChange = { newText ->
                                text = newText
                                saveError = null
                                // Detect if the user just typed "/"
                                if (newText.endsWith("/")) {
                                    showSlashMenu = true
                                } else if (!newText.endsWith("/") && showSlashMenu) {
                                    showSlashMenu = false
                                }
                            },
                            textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink, lineHeight = 28.sp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp).background(JannahColors.Card).padding(16.dp)
                        )
                        
                        if (text.isEmpty()) {
                            Text("What did you do today? Say it plainly. It stays here.\n\nType / to insert Quran or Hadith.", style = JannahType.bodySmall, color = JannahColors.Faint, modifier = Modifier.padding(20.dp))
                        }
                    }

                    // SLASH COMMAND MENU
                    AnimatedVisibility(
                        visible = showSlashMenu,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Column(Modifier.fillMaxWidth().background(JannahColors.Surface).padding(8.dp)) {
                            COMMANDS.forEach { cmd ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        // Replace the trailing "/" with the command content
                                        text = text.dropLast(1) + cmd.content + "\n\n"
                                        showSlashMenu = false
                                    }.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cmd.trigger, style = JannahType.micro, color = JannahColors.Orange, modifier = Modifier.width(72.dp))
                                    Text(cmd.label, style = JannahType.bodySmall, color = JannahColors.Ink)
                                }
                            }
                        }
                    }

                    if (saveError != null) {
                        Text(saveError!!, style = JannahType.bodySmall, color = JannahColors.Danger)
                        Spacer(Modifier.height(JannahSpacing.xs))
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)) {
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Card).clickable { writing = false; text = ""; saveError = null }, contentAlignment = Alignment.Center) { Text("Cancel", style = JannahType.button, color = JannahColors.Muted) }
                        Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(if (text.isBlank()) JannahColors.Border else JannahColors.Orange).clickable {
                            if (text.isNotBlank()) {
                                val saved = runCatching {
                                    val activeStore = store ?: error("Diary storage is unavailable.")
                                    val result = DiaryClassifier.classify(text)
                                    check(activeStore.add(text.trim(), result.tag, result.reactionTitle) > 0)
                                }
                                if (saved.isSuccess) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    text = ""
                                    writing = false
                                    saveError = null
                                    refresh++
                                } else {
                                    saveError = "Your entry wasn't saved. Your writing is still here; try again."
                                }
                            }
                        }, contentAlignment = Alignment.Center) { Text("Save", style = JannahType.button, color = if (text.isBlank()) JannahColors.Faint else JannahColors.OnOrange) }
                    }
                    Spacer(Modifier.height(JannahSpacing.l))
                }

                if (saveError != null && !writing) {
                    Text(saveError!!, style = JannahType.bodySmall, color = JannahColors.Danger)
                    Spacer(Modifier.height(JannahSpacing.s))
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
                            Box(Modifier.clip(RoundedCornerShape(JannahSpacing.pillRadius)).clickable {
                                val deleted = runCatching { store?.delete(e.id) }
                                if (deleted.isSuccess) refresh++ else saveError = "This diary entry couldn't be deleted. Try again."
                            }.padding(6.dp)) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                        }
                        if (i < entries.size - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                } else if (!storeAvailable) {
                    Text("Diary storage couldn't be opened. Your existing entries have not been changed.", style = JannahType.bodySmall, color = JannahColors.Danger, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                } else {
                    Text("Nothing written yet.", style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
                }
                Spacer(Modifier.height(JannahSpacing.xxl))
            }
        }
    }
}
