package com.jannahos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val formattedContent: String? = null, // Stores JSON of the styles
    val updatedAt: Long = System.currentTimeMillis()
)
