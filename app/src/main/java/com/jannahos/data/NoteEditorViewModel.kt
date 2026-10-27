package com.jannahos.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val quranRepository: QuranRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    var editorValue by mutableStateOf(TextFieldValue(""))
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var currentNoteId by mutableStateOf<Long?>(null)
        private set

    fun onEditorValueChange(value: TextFieldValue) {
        val oldText = editorValue.text
        editorValue = value
        
        // Auto-detect if the user just typed a space or newline after a command
        if (value.text.length > oldText.length) {
            val newChar = value.text.lastOrNull()
            if (newChar == ' ' || newChar == '\n') {
                checkForCommands(value)
            }
        }
    }

    private fun checkForCommands(value: TextFieldValue) {
        val text = value.text
        val cursor = value.selection.start
        val prefix = text.substring(0, cursor.coerceIn(0, text.length))
        val regex = Regex("""/quran\s+(\d{1,3}):(\d{1,3})\s+$""") // Matches command ending with space
        
        val match = regex.find(prefix) ?: return
        val surah = match.groupValues[1].toInt()
        val ayah = match.groupValues[2].toInt()
        
        val commandStart = match.range.first
        val commandEnd = cursor
        val originalValue = editorValue

        loading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val verse = quranRepository.getVerse(surah, ayah)
                val result = QuranTextFormatter.insertVerse(
                    currentText = originalValue.annotatedString,
                    commandStart = commandStart,
                    commandEnd = commandEnd,
                    verse = verse
                )
                editorValue = TextFieldValue(
                    annotatedString = result.text,
                    selection = result.selection
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Network error."
            } finally {
                loading = false
            }
        }
    }

    fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNote(id)
            if (note != null) {
                currentNoteId = note.id
                val annotated = if (note.formattedContent != null) {
                    AnnotatedStringSerializer.deserialize(note.formattedContent)
                } else {
                    AnnotatedString(note.content)
                }
                editorValue = TextFieldValue(annotatedString = annotated, selection = androidx.compose.ui.text.TextRange(annotated.length))
            }
        }
    }

    fun saveNote(onSaved: () -> Unit) {
        viewModelScope.launch {
            try {
                val serializedStyles = AnnotatedStringSerializer.serialize(editorValue.annotatedString)
                val newId = noteRepository.saveNote(
                    existingId = currentNoteId,
                    title = editorValue.text.lines().firstOrNull()?.take(30) ?: "Untitled",
                    content = editorValue.text,
                    formattedContent = serializedStyles
                )
                currentNoteId = newId
                onSaved()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }
}
