package com.jannahos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = true)
abstract class JannahDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: JannahDatabase? = null
        fun getDatabase(context: Context): JannahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JannahDatabase::class.java,
                    "jannah_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
