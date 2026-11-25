package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class SleepLog(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE sleeps (
                day TEXT PRIMARY KEY,
                bedtime INTEGER NOT NULL,
                waketime INTEGER NOT NULL,
                quality INTEGER NOT NULL,
                created INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // Keep existing sleep records intact. Future schema changes must use
        // explicit ALTER TABLE migrations rather than drop-and-recreate.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sleeps (
                day TEXT PRIMARY KEY,
                bedtime INTEGER NOT NULL,
                waketime INTEGER NOT NULL,
                quality INTEGER NOT NULL,
                created INTEGER NOT NULL
            )
        """.trimIndent())
    }

    data class Entry(val day: String, val bedtime: Long, val waketime: Long, val quality: Int)

    fun save(bedtimeMillis: Long, waketimeMillis: Long, quality: Int) {
        val v = ContentValues().apply {
            put("day", WakeLog.todayStamp())
            put("bedtime", bedtimeMillis)
            put("waketime", waketimeMillis)
            put("quality", quality)
            put("created", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "sleeps", null, v, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun today(): Entry? {
        readableDatabase.rawQuery(
            "SELECT day, bedtime, waketime, quality FROM sleeps WHERE day = ?",
            arrayOf(WakeLog.todayStamp())
        ).use { c ->
            if (c.moveToFirst()) {
                return Entry(c.getString(0), c.getLong(1), c.getLong(2), c.getInt(3))
            }
        }
        return null
    }

    fun lastNights(n: Int): List<Entry> {
        val out = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            "SELECT day, bedtime, waketime, quality FROM sleeps ORDER BY day DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Entry(c.getString(0), c.getLong(1), c.getLong(2), c.getInt(3)))
            }
        }
        return out
    }

    fun averageHours(n: Int): Float {
        val nights = lastNights(n)
        if (nights.isEmpty()) return 0f
        val total = nights.sumOf { (it.waketime - it.bedtime).toDouble() }
        val avgMs = total / nights.size
        return (avgMs / (1000.0 * 60 * 60)).toFloat()
    }

    companion object {
        private const val DB = "jannah_sleep_enc.db"
    }
}
