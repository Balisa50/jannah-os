package com.jannahos.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes = repository.observeAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun saveNote(existingId: Long?, title: String, content: String, onSaved: (Long) -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val savedId = repository.saveNote(existingId, title, content)
                onSaved(savedId)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun loadNote(id: Long): Note? = repository.getNote(id)
}
