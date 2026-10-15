package com.jannahos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PlannerStore(context: Context) : SQLiteOpenHelper(context, DB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created INTEGER NOT NULL,
                title TEXT NOT NULL,
                anchor TEXT,
                done INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    data class Task(val id: Long, val created: Long, val title: String, val anchor: String?, val done: Boolean)

    fun add(title: String, anchor: String?) {
        val v = ContentValues().apply {
            put("created", System.currentTimeMillis())
            put("title", title)
            if (anchor != null) put("anchor", anchor)
            put("done", 0)
        }
        writableDatabase.insert("tasks", null, v)
    }

    fun toggle(id: Long) {
        writableDatabase.execSQL(
            "UPDATE tasks SET done = CASE WHEN done=0 THEN 1 ELSE 0 END WHERE id = ?",
            arrayOf(id)
        )
    }

    fun delete(id: Long) {
        writableDatabase.delete("tasks", "id = ?", arrayOf(id.toString()))
    }

    fun openTasks(): List<Task> {
        val out = mutableListOf<Task>()
        readableDatabase.rawQuery(
            "SELECT id, created, title, anchor, done FROM tasks WHERE done = 0 ORDER BY created ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Task(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getInt(4) == 1))
            }
        }
        return out
    }

    fun doneToday(): Int {
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM tasks WHERE done = 1 AND created >= ?",
            arrayOf(startOfDay.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    companion object {
        private const val DB = "jannah_planner.db"
        val ANCHORS = listOf("before Fajr", "after Fajr", "after Dhuhr", "after Asr", "after Maghrib", "after Isha", "anytime")
    }
}
