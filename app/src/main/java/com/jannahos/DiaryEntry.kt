package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Personal diary entries. Encrypted at rest. Never transmitted.
// The classifier tags them locally so the app can react intelligently
// without ever sending the text anywhere.
class DiaryStore(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                text TEXT NOT NULL,
                tag TEXT NOT NULL,
                reaction TEXT,
                celebrated INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS entries")
        onCreate(db)
    }

    data class Entry(
        val id: Long,
        val ts: Long,
        val text: String,
        val tag: String,
        val reaction: String?,
        val celebrated: Boolean
    )

    fun add(text: String, tag: String, reaction: String?): Long {
        val v = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("text", text)
            put("tag", tag)
            if (reaction != null) put("reaction", reaction)
            put("celebrated", 0)
        }
        return writableDatabase.insert("entries", null, v)
    }

    fun markCelebrated(id: Long) {
        val v = ContentValues().apply { put("celebrated", 1) }
        writableDatabase.update("entries", v, "id = ?", arrayOf(id.toString()))
    }

    fun recent(n: Int = 30): List<Entry> {
        val out = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            "SELECT id, ts, text, tag, reaction, celebrated FROM entries ORDER BY ts DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Entry(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getInt(5) == 1
                ))
            }
        }
        return out
    }

    // The most recent tag within 24 hours, if any, used by the dashboard.
    fun latestTagIn24h(): Entry? {
        val cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        readableDatabase.rawQuery(
            "SELECT id, ts, text, tag, reaction, celebrated FROM entries WHERE ts > ? ORDER BY ts DESC LIMIT 1",
            arrayOf(cutoff.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                return Entry(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getInt(5) == 1
                )
            }
        }
        return null
    }

    fun delete(id: Long) {
        writableDatabase.delete("entries", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB = "jannah_diary_enc.db"
    }
}
