package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class QuranBookmarkStore(context: Context) : SQLiteOpenHelper(
    context,
    DB,
    DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1,
    0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                surah INTEGER NOT NULL,
                ayah INTEGER NOT NULL,
                note TEXT,
                created INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS bookmarks")
        onCreate(db)
    }

    data class Bookmark(val id: Long, val surah: Int, val ayah: Int, val note: String?, val created: Long)

    fun add(surah: Int, ayah: Int, note: String? = null) {
        val v = ContentValues().apply {
            put("surah", surah)
            put("ayah", ayah)
            if (note != null) put("note", note)
            put("created", System.currentTimeMillis())
        }
        writableDatabase.insert("bookmarks", null, v)
    }

    fun remove(surah: Int, ayah: Int) {
        writableDatabase.delete(
            "bookmarks", "surah = ? AND ayah = ?",
            arrayOf(surah.toString(), ayah.toString())
        )
    }

    fun isBookmarked(surah: Int, ayah: Int): Boolean {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM bookmarks WHERE surah = ? AND ayah = ?",
            arrayOf(surah.toString(), ayah.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) > 0 }
        return false
    }

    fun all(): List<Bookmark> {
        val out = mutableListOf<Bookmark>()
        readableDatabase.rawQuery(
            "SELECT id, surah, ayah, note, created FROM bookmarks ORDER BY created DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Bookmark(c.getLong(0), c.getInt(1), c.getInt(2), c.getString(3), c.getLong(4)))
            }
        }
        return out
    }

    companion object {
        private const val DB = "jannah_quran_marks_enc.db"
    }
}
