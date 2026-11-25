package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class StruggleLog(context: Context) : SQLiteOpenHelper(
    context,
    DB,
    DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    2,
    0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {

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
        // Preserve existing records. This table has no schema changes in v2;
        // IF NOT EXISTS also repairs a missing table without deleting history.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                kind TEXT NOT NULL,
                note TEXT
            )
        """.trimIndent())
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
        private const val DB = "jannah_struggle_enc.db"
    }
}
