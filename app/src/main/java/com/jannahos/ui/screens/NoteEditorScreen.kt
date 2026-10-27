package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannahos.data.JannahDatabase
import com.jannahos.data.NoteEditorViewModel
import com.jannahos.data.NoteRepository
import com.jannahos.data.QuranRepository
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType

@Composable
fun NoteEditorScreen(noteId: Long?, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val db = remember { JannahDatabase.getDatabase(ctx) }
    val noteRepo = remember { NoteRepository(db.noteDao()) }
    val quranRepo = remember { QuranRepository() }

    val factory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return NoteEditorViewModel(quranRepo, noteRepo) as T
            }
        }
    }

    val viewModel: NoteEditorViewModel = viewModel(factory = factory)
    val editorValue = viewModel.editorValue
    val loading = viewModel.loading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(noteId) {
        if (noteId != null) viewModel.loadNote(noteId)
    }

    Column(modifier = Modifier.fillMaxSize().background(JannahColors.Cream).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Back", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.weight(1f))
            if (loading) {
                Text("Fetching...", style = JannahType.caption, color = JannahColors.Orange, modifier = Modifier.padding(end = 12.dp))
            }
            Text("Save", style = JannahType.button, color = JannahColors.Orange, modifier = Modifier.clickable {
                viewModel.saveNote { onBack() }
            })
        }

        BasicTextField(
            value = editorValue,
            onValueChange = viewModel::onEditorValueChange,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp).padding(top = 8.dp),
            textStyle = TextStyle(fontSize = 17.sp, lineHeight = 29.sp, color = JannahColors.Ink),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxSize()) {
                    if (editorValue.text.isEmpty()) {
                        Text(
                            "Start writing...\n\nJust type /quran 2:255 and press space.",
                            color = JannahColors.Faint,
                            fontSize = 17.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        errorMessage?.let {
            Text(it, color = JannahColors.Danger, modifier = Modifier.padding(16.dp))
        }
    }
}
