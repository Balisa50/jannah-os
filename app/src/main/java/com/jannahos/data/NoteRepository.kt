package com.jannahos.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun observeAllNotes(): Flow<List<Note>> = dao.observeAllNotes()
    suspend fun getNote(id: Long): Note? = dao.getNoteById(id)

    suspend fun saveNote(existingId: Long?, title: String, content: String, formattedContent: String?): Long {
        return if (existingId == null) {
            dao.insertNote(Note(title = title, content = content, formattedContent = formattedContent))
        } else {
            val existingNote = dao.getNoteById(existingId) ?: throw IllegalStateException("Note not found")
            val updatedNote = existingNote.copy(title = title, content = content, formattedContent = formattedContent, updatedAt = System.currentTimeMillis())
            dao.updateNote(updatedNote)
            existingId
        }
    }
}
