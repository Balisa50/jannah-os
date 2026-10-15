package com.jannahos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WakeLog(context: Context) : SQLiteOpenHelper(context, DB, null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                outcome TEXT NOT NULL,
                reason TEXT,
                voice_path TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS wake_events")
        onCreate(db)
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

    data class Event(val ts: Long, val outcome: String, val reason: String?, val voicePath: String?)

    fun lastNights(n: Int = 7): List<Event> {
        val out = mutableListOf<Event>()
        readableDatabase.rawQuery(
            "SELECT ts, outcome, reason, voice_path FROM events ORDER BY ts DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Event(c.getLong(0), c.getString(1), c.getString(2), c.getString(3)))
            }
        }
        return out
    }

    fun commonReason(): String? {
        readableDatabase.rawQuery(
            "SELECT reason, COUNT(*) as cnt FROM events WHERE reason IS NOT NULL AND ts > ? GROUP BY reason ORDER BY cnt DESC LIMIT 1",
            arrayOf((System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000).toString())
        ).use { c ->
            if (c.moveToFirst()) return c.getString(0)
        }
        return null
    }

    companion object {
        private const val DB = "jannah_v2.db"
    }
}
