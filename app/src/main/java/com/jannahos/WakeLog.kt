package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class WakeLog(context: Context) : SQLiteOpenHelper(
    context,
    DB,
    DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    3,
    0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                outcome TEXT NOT NULL,
                reason TEXT,
                voice_path TEXT,
                verified INTEGER NOT NULL DEFAULT 0,
                transcript TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // Never discard a user's history during an app update. Older versions
        // already have the core events table; add only columns introduced later.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                outcome TEXT NOT NULL,
                reason TEXT,
                voice_path TEXT,
                verified INTEGER NOT NULL DEFAULT 0,
                transcript TEXT
            )
        """.trimIndent())
        ensureColumn(db, "reason", "TEXT")
        ensureColumn(db, "voice_path", "TEXT")
        ensureColumn(db, "verified", "INTEGER NOT NULL DEFAULT 0")
        ensureColumn(db, "transcript", "TEXT")
    }

    private fun ensureColumn(db: SQLiteDatabase, name: String, definition: String) {
        var exists = false
        db.rawQuery("PRAGMA table_info(events)", null).use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == name) {
                    exists = true
                    break
                }
            }
        }
        if (!exists) db.execSQL("ALTER TABLE events ADD COLUMN $name $definition")
    }

    fun record(outcome: String, reason: String? = null, voicePath: String? = null) {
        val values = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("outcome", outcome)
            if (reason != null) put("reason", reason)
            if (voicePath != null) put("voice_path", voicePath)
        }
        writableDatabase.insert("events", null, values)
    }

    fun recordVerified(outcome: String, verified: Boolean, transcript: String?) {
        val values = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("outcome", outcome)
            put("verified", if (verified) 1 else 0)
            if (transcript != null) put("transcript", transcript)
        }
        writableDatabase.insert("events", null, values)
    }

    data class Event(val id: Long, val ts: Long, val outcome: String, val reason: String?, val voicePath: String?)

    fun lastNights(n: Int = 7): List<Event> {
        val out = mutableListOf<Event>()
        readableDatabase.rawQuery(
            "SELECT id, ts, outcome, reason, voice_path FROM events ORDER BY ts DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Event(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getString(4)))
            }
        }
        return out
    }

    fun pendingFollowUp(): Event? {
        readableDatabase.rawQuery(
            "SELECT id, ts, outcome, reason, voice_path FROM events WHERE outcome='missed' AND (reason IS NULL OR reason='not_now') ORDER BY ts DESC LIMIT 1",
            null
        ).use { c ->
            if (c.moveToFirst()) {
                return Event(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getString(4))
            }
        }
        return null
    }

    fun setReason(id: Long, reason: String) {
        val values = ContentValues().apply { put("reason", reason) }
        writableDatabase.update("events", values, "id = ?", arrayOf(id.toString()))
    }

    fun commonReason(): String? {
        readableDatabase.rawQuery(
            "SELECT reason, COUNT(*) as cnt FROM events WHERE reason IS NOT NULL AND reason != 'not_now' AND ts > ? GROUP BY reason ORDER BY cnt DESC LIMIT 1",
            arrayOf((System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000).toString())
        ).use { c ->
            if (c.moveToFirst()) return c.getString(0)
        }
        return null
    }

    fun prayedCount(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM events WHERE outcome='prayed'",
            null
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    data class FormationStats(
        val verified: Int,
        val claimed: Int,
        val missed: Int,
        val total: Int,
        val verificationRate: Float
    )

    fun formationStats(days: Int = 30): FormationStats {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        var verified = 0
        var claimed = 0
        var missed = 0
        readableDatabase.rawQuery(
            "SELECT outcome, verified FROM events WHERE ts > ?",
            arrayOf(cutoff.toString())
        ).use { c ->
            while (c.moveToNext()) {
                when (c.getString(0)) {
                    "prayed" -> if (c.getInt(1) == 1) verified++ else claimed++
                    "missed" -> missed++
                }
            }
        }
        val total = verified + claimed + missed
        val rate = if (verified + claimed > 0) verified.toFloat() / (verified + claimed).toFloat() else 0f
        return FormationStats(verified, claimed, missed, total, rate)
    }

    companion object {
        private const val DB = "jannah_v3_enc.db"

        fun todayStamp(): String {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            return fmt.format(java.util.Date())
        }
    }


}
