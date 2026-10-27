package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jannahos.data.JannahDatabase
import com.jannahos.data.NoteListViewModel
import com.jannahos.data.NoteRepository
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesListScreen(onBack: () -> Unit, onOpenNote: (Long?) -> Unit) {
    val ctx = LocalContext.current
    val db = remember { JannahDatabase.getDatabase(ctx) }
    val repo = remember { NoteRepository(db.noteDao()) }
    val viewModel = remember { NoteListViewModel(repo) }
    val notes by viewModel.notes.collectAsState()
    
    val dayFmt = SimpleDateFormat("EEE d MMM", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Notes", eyebrow = "PRIVATE", subtitle = "Write it down.", onBack = onBack)

        Column(modifier = Modifier.weight(1f).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable { onOpenNote(null) }, contentAlignment = Alignment.Center) { 
                Text("New Note", style = JannahType.button, color = JannahColors.OnOrange) 
            }
            
            Spacer(Modifier.height(JannahSpacing.l))

            if (notes.isNotEmpty()) {
                Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                LazyColumn {
                    items(notes, key = { it.id }) { note ->
                        Column(Modifier.fillMaxWidth().clickable { onOpenNote(note.id) }.padding(vertical = 12.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(dayFmt.format(Date(note.updatedAt)), style = JannahType.micro, color = JannahColors.Faint, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(note.title.ifBlank { "Untitled" }, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JannahColors.Ink)
                            Text(note.content.take(100), style = JannahType.bodySmall, color = JannahColors.Muted, maxLines = 2, lineHeight = 20.sp)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            } else {
                Text("Nothing written yet.", style = JannahType.bodySmall, color = JannahColors.Muted, modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl))
            }
        }
    }
}
