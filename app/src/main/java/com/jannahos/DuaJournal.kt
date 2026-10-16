package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Personal record of duas and their answers.
// The user names what they are asking for. When Allah answers, they record it.
// Over months, this becomes an honest archive of answered duas, a lifeline
// in moments of doubt or hardship.
class DuaJournalStore(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE duas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created INTEGER NOT NULL,
                category TEXT NOT NULL,
                ask TEXT NOT NULL,
                answered INTEGER NOT NULL DEFAULT 0,
                answered_at INTEGER,
                answer_note TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS duas")
        onCreate(db)
    }

    data class Dua(
        val id: Long, val created: Long, val category: String,
        val ask: String, val answered: Boolean,
        val answeredAt: Long?, val answerNote: String?
    )

    fun add(category: String, ask: String): Long {
        val v = ContentValues().apply {
            put("created", System.currentTimeMillis())
            put("category", category)
            put("ask", ask)
            put("answered", 0)
        }
        return writableDatabase.insert("duas", null, v)
    }

    fun markAnswered(id: Long, note: String?) {
        val v = ContentValues().apply {
            put("answered", 1)
            put("answered_at", System.currentTimeMillis())
            if (note != null) put("answer_note", note)
        }
        writableDatabase.update("duas", v, "id = ?", arrayOf(id.toString()))
    }

    fun unmarkAnswered(id: Long) {
        val v = ContentValues().apply {
            put("answered", 0)
            putNull("answered_at")
            putNull("answer_note")
        }
        writableDatabase.update("duas", v, "id = ?", arrayOf(id.toString()))
    }

    fun all(): List<Dua> {
        val out = mutableListOf<Dua>()
        readableDatabase.rawQuery(
            "SELECT id, created, category, ask, answered, answered_at, answer_note FROM duas ORDER BY answered ASC, created DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Dua(
                    c.getLong(0), c.getLong(1), c.getString(2), c.getString(3),
                    c.getInt(4) == 1,
                    if (c.isNull(5)) null else c.getLong(5),
                    c.getString(6)
                ))
            }
        }
        return out
    }

    fun answeredCount(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM duas WHERE answered = 1", null
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    fun pendingCount(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM duas WHERE answered = 0", null
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    fun delete(id: Long) {
        writableDatabase.delete("duas", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB = "jannah_dua_journal_enc.db"

        val CATEGORIES = listOf(
            "rizq" to "Rizq / provision",
            "health" to "Health",
            "family" to "Family",
            "marriage" to "Marriage",
            "children" to "Children",
            "guidance" to "Guidance / hidayah",
            "forgiveness" to "Forgiveness",
            "hardship" to "Hardship relief",
            "exam" to "Study / exam",
            "work" to "Work",
            "ummah" to "For the ummah",
            "other" to "Other"
        )

        fun label(code: String): String =
            CATEGORIES.firstOrNull { it.first == code }?.second ?: code
    }
}
