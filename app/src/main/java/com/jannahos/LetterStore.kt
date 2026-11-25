package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Letters to your future self. Encrypted. Sealed until the chosen date.
// Not read-able before then, not by you, not by us.
class LetterStore(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE letters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created INTEGER NOT NULL,
                opens_at INTEGER NOT NULL,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                opened INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // Preserve sealed letters. Future changes must use explicit migrations.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS letters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created INTEGER NOT NULL,
                opens_at INTEGER NOT NULL,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                opened INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    data class Letter(
        val id: Long, val created: Long, val opensAt: Long,
        val title: String, val body: String, val opened: Boolean
    )

    fun add(title: String, body: String, daysAhead: Int): Long {
        val now = System.currentTimeMillis()
        val opens = now + daysAhead * 24L * 60 * 60 * 1000
        val v = ContentValues().apply {
            put("created", now)
            put("opens_at", opens)
            put("title", title)
            put("body", body)
            put("opened", 0)
        }
        return writableDatabase.insert("letters", null, v)
    }

    fun all(): List<Letter> {
        val out = mutableListOf<Letter>()
        readableDatabase.rawQuery(
            "SELECT id, created, opens_at, title, body, opened FROM letters ORDER BY opens_at ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Letter(
                    c.getLong(0), c.getLong(1), c.getLong(2),
                    c.getString(3), c.getString(4), c.getInt(5) == 1
                ))
            }
        }
        return out
    }

    fun ready(): List<Letter> {
        val now = System.currentTimeMillis()
        return all().filter { !it.opened && it.opensAt <= now }
    }

    fun sealed(): List<Letter> {
        val now = System.currentTimeMillis()
        return all().filter { it.opensAt > now }
    }

    fun markOpened(id: Long) {
        val v = ContentValues().apply { put("opened", 1) }
        writableDatabase.update("letters", v, "id = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("letters", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB = "jannah_letters_enc.db"
    }
}
