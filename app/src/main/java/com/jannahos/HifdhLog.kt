package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Tracks memorization progress. Spaced repetition interval in days:
// 1, 3, 7, 14, 30, 60, 120, 365, then infinite.
class HifdhLog(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE hifdh (
                surah INTEGER NOT NULL,
                ayah INTEGER NOT NULL,
                memorised INTEGER NOT NULL DEFAULT 0,
                stage INTEGER NOT NULL DEFAULT 0,
                last_review INTEGER NOT NULL DEFAULT 0,
                next_review INTEGER NOT NULL DEFAULT 0,
                created INTEGER NOT NULL,
                PRIMARY KEY (surah, ayah)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS hifdh")
        onCreate(db)
    }

    data class Entry(
        val surah: Int,
        val ayah: Int,
        val memorised: Boolean,
        val stage: Int,
        val lastReview: Long,
        val nextReview: Long
    )

    // Interval per stage in days.
    private val STAGE_DAYS = listOf(1L, 3L, 7L, 14L, 30L, 60L, 120L, 365L, 3650L)

    fun mark(surah: Int, ayah: Int) {
        val now = System.currentTimeMillis()
        val next = now + STAGE_DAYS[0] * 24 * 60 * 60 * 1000
        val v = ContentValues().apply {
            put("surah", surah)
            put("ayah", ayah)
            put("memorised", 1)
            put("stage", 0)
            put("last_review", now)
            put("next_review", next)
            put("created", now)
        }
        writableDatabase.insertWithOnConflict(
            "hifdh", null, v, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun unmark(surah: Int, ayah: Int) {
        writableDatabase.delete("hifdh", "surah = ? AND ayah = ?",
            arrayOf(surah.toString(), ayah.toString()))
    }

    // User reports a revision. Advance stage, schedule next.
    fun review(surah: Int, ayah: Int, well: Boolean) {
        readableDatabase.rawQuery(
            "SELECT stage FROM hifdh WHERE surah = ? AND ayah = ?",
            arrayOf(surah.toString(), ayah.toString())
        ).use { c ->
            if (!c.moveToFirst()) return
            val stage = c.getInt(0)
            val newStage = if (well) (stage + 1).coerceAtMost(STAGE_DAYS.size - 1) else 0
            val now = System.currentTimeMillis()
            val next = now + STAGE_DAYS[newStage] * 24 * 60 * 60 * 1000
            val v = ContentValues().apply {
                put("stage", newStage)
                put("last_review", now)
                put("next_review", next)
            }
            writableDatabase.update("hifdh", v, "surah = ? AND ayah = ?",
                arrayOf(surah.toString(), ayah.toString()))
        }
    }

    fun isMemorised(surah: Int, ayah: Int): Boolean {
        readableDatabase.rawQuery(
            "SELECT memorised FROM hifdh WHERE surah = ? AND ayah = ?",
            arrayOf(surah.toString(), ayah.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) == 1 }
        return false
    }

    // Returns all ayahs due for revision right now.
    fun dueNow(): List<Entry> {
        val out = mutableListOf<Entry>()
        val now = System.currentTimeMillis()
        readableDatabase.rawQuery(
            "SELECT surah, ayah, memorised, stage, last_review, next_review FROM hifdh WHERE memorised = 1 AND next_review <= ? ORDER BY next_review ASC",
            arrayOf(now.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Entry(c.getInt(0), c.getInt(1), c.getInt(2) == 1, c.getInt(3), c.getLong(4), c.getLong(5)))
            }
        }
        return out
    }

    fun totalMemorised(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM hifdh WHERE memorised = 1", null
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    fun memorisedInSurah(surah: Int): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM hifdh WHERE memorised = 1 AND surah = ?",
            arrayOf(surah.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    companion object {
        private const val DB = "jannah_hifdh_enc.db"
    }
}
