package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Private tracker for habits the user is trying to leave.
// Labels are chosen from neutral codes (Habit A/B/C) or user-typed words
// the user controls. Never shared. Never uploaded.
// No streaks that shame, clean-day count is informational only.
class StruggleTracker(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                label TEXT NOT NULL,
                started INTEGER NOT NULL,
                last_reset INTEGER NOT NULL,
                best_streak INTEGER NOT NULL DEFAULT 0,
                current_streak INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE slips (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER NOT NULL,
                ts INTEGER NOT NULL,
                reason TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS habits")
        db.execSQL("DROP TABLE IF EXISTS slips")
        onCreate(db)
    }

    data class Habit(
        val id: Long,
        val label: String,
        val started: Long,
        val lastReset: Long,
        val bestStreak: Int,
        val currentStreak: Int
    )

    fun addHabit(label: String): Long {
        val now = System.currentTimeMillis()
        val v = ContentValues().apply {
            put("label", label.trim())
            put("started", now)
            put("last_reset", now)
            put("best_streak", 0)
            put("current_streak", 0)
        }
        return writableDatabase.insert("habits", null, v)
    }

    fun habits(): List<Habit> {
        val out = mutableListOf<Habit>()
        readableDatabase.rawQuery(
            "SELECT id, label, started, last_reset, best_streak, current_streak FROM habits ORDER BY id ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Habit(
                    c.getLong(0), c.getString(1), c.getLong(2), c.getLong(3),
                    c.getInt(4), c.getInt(5)
                ))
            }
        }
        return out
    }

    // Days since last reset.
    fun currentDays(habit: Habit): Int {
        val ms = System.currentTimeMillis() - habit.lastReset
        return (ms / (24L * 60 * 60 * 1000)).toInt()
    }

    fun recordSlip(habitId: Long, reason: String?): Int {
        // Record slip.
        val now = System.currentTimeMillis()
        val v = ContentValues().apply {
            put("habit_id", habitId)
            put("ts", now)
            if (reason != null) put("reason", reason)
        }
        writableDatabase.insert("slips", null, v)

        // Update streaks: current best preserved, current resets to 0, last_reset moves.
        readableDatabase.rawQuery(
            "SELECT current_streak, best_streak FROM habits WHERE id = ?",
            arrayOf(habitId.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                val current = c.getInt(0)
                val best = c.getInt(1)
                val newBest = maxOf(best, current)
                val upd = ContentValues().apply {
                    put("last_reset", now)
                    put("current_streak", 0)
                    put("best_streak", newBest)
                }
                writableDatabase.update("habits", upd, "id = ?", arrayOf(habitId.toString()))
                return newBest
            }
        }
        return 0
    }

    fun updateStreak(habitId: Long, days: Int) {
        val v = ContentValues().apply { put("current_streak", days) }
        writableDatabase.update("habits", v, "id = ?", arrayOf(habitId.toString()))
    }

    fun deleteHabit(id: Long) {
        writableDatabase.delete("habits", "id = ?", arrayOf(id.toString()))
        writableDatabase.delete("slips", "habit_id = ?", arrayOf(id.toString()))
    }

    fun slipsFor(habitId: Long, days: Int): List<Long> {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        val out = mutableListOf<Long>()
        readableDatabase.rawQuery(
            "SELECT ts FROM slips WHERE habit_id = ? AND ts > ? ORDER BY ts DESC",
            arrayOf(habitId.toString(), cutoff.toString())
        ).use { c ->
            while (c.moveToNext()) out.add(c.getLong(0))
        }
        return out
    }

    companion object {
        private const val DB = "jannah_struggle_tracker_enc.db"
    }
}
