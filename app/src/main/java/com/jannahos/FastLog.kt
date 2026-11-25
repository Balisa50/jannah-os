package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class FastLog(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE fasts (
                day TEXT PRIMARY KEY,
                kind TEXT NOT NULL,
                created INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // The v1 schema remains compatible. Never erase a user's fast history
        // simply because a future app version increments the database version.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS fasts (
                day TEXT PRIMARY KEY,
                kind TEXT NOT NULL,
                created INTEGER NOT NULL
            )
        """.trimIndent())
    }

    fun markToday(kind: String) {
        val v = ContentValues().apply {
            put("day", WakeLog.todayStamp())
            put("kind", kind)
            put("created", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "fasts", null, v, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun unmarkToday() {
        writableDatabase.delete("fasts", "day = ?", arrayOf(WakeLog.todayStamp()))
    }

    fun todayKind(): String? {
        readableDatabase.rawQuery(
            "SELECT kind FROM fasts WHERE day = ?",
            arrayOf(WakeLog.todayStamp())
        ).use { c -> if (c.moveToFirst()) return c.getString(0) }
        return null
    }

    fun countLast(days: Int): Int {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM fasts WHERE created > ?",
            arrayOf(cutoff.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    companion object {
        private const val DB = "jannah_fast_enc.db"

        val KINDS = listOf(
            "ramadan" to "Ramadan",
            "monday" to "Monday fast",
            "thursday" to "Thursday fast",
            "white_days" to "White days (13/14/15)",
            "arafah" to "Day of Arafah",
            "ashura" to "Day of Ashura",
            "voluntary" to "Voluntary"
        )

        fun label(code: String): String =
            KINDS.firstOrNull { it.first == code }?.second ?: code
    }
}
