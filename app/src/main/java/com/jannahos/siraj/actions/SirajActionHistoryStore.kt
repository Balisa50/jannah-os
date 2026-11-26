package com.jannahos.siraj.actions

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import com.jannahos.DbKey

/**
 * Encrypted, on-device audit trail for actions the user explicitly approved.
 * It stores only the action label/summary and outcome, never provider prompts,
 * API credentials, or general conversation history.
 */
class SirajActionHistoryStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DB_NAME,
    DbKey.get(context.applicationContext),
    null as SQLiteDatabase.CursorFactory?,
    1,
    0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    data class Entry(
        val id: Long,
        val createdAt: Long,
        val actionType: String,
        val summary: String,
        val status: String,
        val details: String?
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE action_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                summary TEXT NOT NULL,
                status TEXT NOT NULL,
                details TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_action_history_created ON action_history(created_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS action_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                summary TEXT NOT NULL,
                status TEXT NOT NULL,
                details TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_action_history_created ON action_history(created_at DESC)"
        )
    }

    fun record(
        action: SirajDeviceAction,
        summary: String,
        result: ActionExecutionResult
    ) {
        val status: String
        val details: String?
        when (result) {
            ActionExecutionResult.Completed -> {
                status = "completed"
                details = "Android accepted the action."
            }
            ActionExecutionResult.NeedsConfirmation -> {
                status = "needs_confirmation"
                details = "Execution requested confirmation again."
            }
            is ActionExecutionResult.Blocked -> {
                status = "blocked"
                details = result.reason
            }
            is ActionExecutionResult.Failed -> {
                status = "failed"
                details = result.reason
            }
        }
        insert(action, summary, status, details)
    }

    fun recordCancelled(action: SirajDeviceAction, summary: String) {
        insert(action, summary, "cancelled", "You cancelled before execution.")
    }

    fun recent(limit: Int = 10): List<Entry> {
        val entries = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            """
            SELECT id, created_at, action_type, summary, status, details
            FROM action_history
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.coerceIn(1, 50).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entries += Entry(
                    id = cursor.getLong(0),
                    createdAt = cursor.getLong(1),
                    actionType = cursor.getString(2),
                    summary = cursor.getString(3),
                    status = cursor.getString(4),
                    details = cursor.getString(5)
                )
            }
        }
        return entries
    }

    fun clear() {
        writableDatabase.delete("action_history", null, null)
    }

    private fun insert(
        action: SirajDeviceAction,
        summary: String,
        status: String,
        details: String?
    ) {
        val actionType = when (action) {
            is SirajDeviceAction.ScheduleReminder -> "reminder"
            is SirajDeviceAction.SetPrayerLock -> "prayer_lock"
            is SirajDeviceAction.SetQuranLock -> "quran_lock"
            is SirajDeviceAction.SetBedtimeLock -> "bedtime_lock"
            is SirajDeviceAction.SetDoNotDisturb -> "do_not_disturb"
        }
        val values = ContentValues().apply {
            put("created_at", System.currentTimeMillis())
            put("action_type", actionType)
            put("summary", summary.trim().take(160))
            put("status", status)
            if (details != null) put("details", details.take(300))
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.insertOrThrow("action_history", null, values)
            // Keep a bounded history to avoid unbounded growth of private data.
            db.execSQL(
                """
                DELETE FROM action_history
                WHERE id NOT IN (
                    SELECT id FROM action_history
                    ORDER BY created_at DESC, id DESC
                    LIMIT 100
                )
                """.trimIndent()
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val DB_NAME = "siraj_action_history_enc.db"
    }
}
