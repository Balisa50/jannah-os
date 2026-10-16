package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Tracks which interventions Right Now suggested, and whether the user acted.
// Over weeks this lets the app quietly prioritise what actually moves you,
// rather than sending the same generic nudge every day.
// No points. No gamification. Just observed patterns.
class InterventionLog(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE interventions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                kind TEXT NOT NULL,
                acted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS interventions")
        onCreate(db)
    }

    fun logShown(kind: String): Long {
        val v = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("kind", kind)
            put("acted", 0)
        }
        return writableDatabase.insert("interventions", null, v)
    }

    fun markActed(id: Long) {
        val v = ContentValues().apply { put("acted", 1) }
        writableDatabase.update("interventions", v, "id = ?", arrayOf(id.toString()))
    }

    // The most recent unacted intervention of a given kind, if any.
    // Used to detect whether the user tapped the action within the window.
    fun lastUnacted(kind: String): Long? {
        val cutoff = System.currentTimeMillis() - 30L * 60 * 1000 // 30 min window
        readableDatabase.rawQuery(
            "SELECT id FROM interventions WHERE kind = ? AND acted = 0 AND ts > ? ORDER BY ts DESC LIMIT 1",
            arrayOf(kind, cutoff.toString())
        ).use { c -> if (c.moveToFirst()) return c.getLong(0) }
        return null
    }

    data class KindStat(val kind: String, val shown: Int, val acted: Int) {
        val rate: Float get() = if (shown == 0) 0f else acted.toFloat() / shown.toFloat()
    }

    fun stats(days: Int = 30): List<KindStat> {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        val shown = mutableMapOf<String, Int>()
        val acted = mutableMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT kind, acted FROM interventions WHERE ts > ?",
            arrayOf(cutoff.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val k = c.getString(0)
                shown[k] = (shown[k] ?: 0) + 1
                if (c.getInt(1) == 1) acted[k] = (acted[k] ?: 0) + 1
            }
        }
        return shown.map { (k, s) ->
            KindStat(k, s, acted[k] ?: 0)
        }.sortedByDescending { it.rate }
    }

    // The kind the user responds to most (with at least 3 shown).
    fun bestKind(): String? =
        stats(30).firstOrNull { it.shown >= 3 && it.rate >= 0.4f }?.kind

    companion object {
        private const val DB = "jannah_interventions_enc.db"

        fun label(kind: String): String = when (kind) {
            "imminent_prayer" -> "prayer prompts"
            "pending_prayer" -> "missed-prayer reminders"
            "tahajjud" -> "tahajjud calls"
            "bedtime" -> "bedtime nudges"
            "dhikr" -> "dhikr invitations"
            "quran" -> "Quran prompts"
            else -> kind
        }
    }
}
