package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class SadaqahLog(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE gifts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL,
                kind TEXT NOT NULL,
                note TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS gifts")
        onCreate(db)
    }

    data class Gift(
        val id: Long, val ts: Long, val amount: Double,
        val currency: String, val kind: String, val note: String?
    )

    fun add(amount: Double, currency: String, kind: String, note: String?): Long {
        val v = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("amount", amount)
            put("currency", currency)
            put("kind", kind)
            if (note != null) put("note", note)
        }
        return writableDatabase.insert("gifts", null, v)
    }

    fun recent(n: Int = 40): List<Gift> {
        val out = mutableListOf<Gift>()
        readableDatabase.rawQuery(
            "SELECT id, ts, amount, currency, kind, note FROM gifts ORDER BY ts DESC LIMIT ?",
            arrayOf(n.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Gift(c.getLong(0), c.getLong(1), c.getDouble(2),
                    c.getString(3), c.getString(4), c.getString(5)))
            }
        }
        return out
    }

    fun totalByCurrency(): Map<String, Double> {
        val out = mutableMapOf<String, Double>()
        readableDatabase.rawQuery(
            "SELECT currency, SUM(amount) FROM gifts GROUP BY currency", null
        ).use { c ->
            while (c.moveToNext()) out[c.getString(0)] = c.getDouble(1)
        }
        return out
    }

    fun countLast30(): Int {
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM gifts WHERE ts > ?",
            arrayOf(cutoff.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) }
        return 0
    }

    fun delete(id: Long) {
        writableDatabase.delete("gifts", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB = "jannah_sadaqah_enc.db"

        val KINDS = listOf(
            "sadaqah" to "Sadaqah (general)",
            "zakat" to "Zakat",
            "zakat_al_fitr" to "Zakat al-Fitr",
            "fidya" to "Fidya",
            "kaffarah" to "Kaffarah",
            "orphan" to "Orphan sponsorship",
            "mosque" to "Masjid",
            "family" to "Family support"
        )

        fun label(code: String): String =
            KINDS.firstOrNull { it.first == code }?.second ?: code
    }
}
