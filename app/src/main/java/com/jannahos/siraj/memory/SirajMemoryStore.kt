package com.jannahos.siraj.memory

import android.content.ContentValues
import android.content.Context
import com.jannahos.DbKey
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

/**
 * User-authored private memory. Entries remain on-device in an encrypted
 * SQLCipher database and are never added to provider prompts automatically.
 */
class SirajMemoryStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DB_NAME,
    DbKey.get(context.applicationContext),
    null as SQLiteDatabase.CursorFactory?,
    1,
    0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    data class Entry(
        val id: Long,
        val createdAt: Long,
        val content: String
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE memories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                content TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_memories_created ON memories(created_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS memories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                content TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_memories_created ON memories(created_at DESC)"
        )
    }

    /** Returns false for blank/duplicate input; otherwise persists the entry. */
    fun add(rawContent: String): Boolean {
        val content = rawContent.trim().take(MAX_CONTENT_CHARS)
        if (content.isBlank()) return false
        if (recent(MAX_ITEMS).any { it.content.equals(content, ignoreCase = true) }) {
            return false
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("created_at", System.currentTimeMillis())
                put("content", content)
            }
            db.insertOrThrow("memories", null, values)
            db.execSQL(
                """
                DELETE FROM memories
                WHERE id NOT IN (
                    SELECT id FROM memories
                    ORDER BY created_at DESC, id DESC
                    LIMIT $MAX_ITEMS
                )
                """.trimIndent()
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return true
    }

    fun recent(limit: Int = 20): List<Entry> {
        val entries = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            """
            SELECT id, created_at, content
            FROM memories
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.coerceIn(1, MAX_ITEMS).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entries += Entry(
                    id = cursor.getLong(0),
                    createdAt = cursor.getLong(1),
                    content = cursor.getString(2)
                )
            }
        }
        return entries
    }

    fun search(query: String): List<Entry> {
        val needle = query.trim()
        if (needle.isBlank()) return emptyList()
        return recent(MAX_ITEMS).filter { it.content.contains(needle, ignoreCase = true) }
    }

    fun delete(id: Long): Boolean =
        writableDatabase.delete("memories", "id = ?", arrayOf(id.toString())) > 0

    fun clear() {
        writableDatabase.delete("memories", null, null)
    }

    companion object {
        private const val DB_NAME = "siraj_private_memory_enc.db"
        private const val MAX_ITEMS = 100
        private const val MAX_CONTENT_CHARS = 500
    }
}
