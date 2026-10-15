package com.jannahos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StruggleLog(context: Context) : SQLiteOpenHelper(context, DB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                kind TEXT NOT NULL,
                note TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS entries")
        onCreate(db)
    }

    fun record(kind: String, note: String? = null) {
        val values = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("kind", kind)
            if (note != null) put("note", note)
        }
        writableDatabase.insert("entries", null, values)
    }

    data class Entry(val ts: Long, val kind: String, val note: String?)

    fun recent(n: Int = 20): List<Entry> {
        val out = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            "SELECT ts, kind, note FROM entries ORDER BY ts DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Entry(c.getLong(0), c.getString(1), c.getString(2)))
            }
        }
        return out
    }

    fun countSince(kind: String, sinceMs: Long): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM entries WHERE kind = ? AND ts > ?",
            arrayOf(kind, sinceMs.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    fun deleteAll() {
        writableDatabase.delete("entries", null, null)
    }

    companion object {
        private const val DB = "jannah_struggle.db"
    }
}
