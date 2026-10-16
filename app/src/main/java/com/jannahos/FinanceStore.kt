package com.jannahos

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

// Monthly finance tracking with the 0.5x / 1.5x salary framework.
// Everything stored on-device. No bank integration.
class FinanceStore(context: Context) : SQLiteOpenHelper(
    context, DB, DbKey.get(context),
    null as SQLiteDatabase.CursorFactory?,
    1, 0,
    null as DatabaseErrorHandler?,
    null as SQLiteDatabaseHook?,
    false
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE months (
                month TEXT PRIMARY KEY,
                salary REAL NOT NULL,
                currency TEXT NOT NULL,
                updated INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                note TEXT,
                ts INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS months")
        db.execSQL("DROP TABLE IF EXISTS expenses")
        onCreate(db)
    }

    data class Expense(
        val id: Long, val month: String, val category: String,
        val amount: Double, val note: String?, val ts: Long
    )

    data class Month(
        val month: String, val salary: Double, val currency: String
    )

    // monthKey format: "2026-10"
    fun currentMonthKey(): String {
        val cal = java.util.Calendar.getInstance()
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        return "%04d-%02d".format(y, m)
    }

    fun setSalary(month: String, salary: Double, currency: String) {
        val v = ContentValues().apply {
            put("month", month)
            put("salary", salary)
            put("currency", currency)
            put("updated", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "months", null, v, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun month(month: String): Month? {
        readableDatabase.rawQuery(
            "SELECT month, salary, currency FROM months WHERE month = ?",
            arrayOf(month)
        ).use { c ->
            if (c.moveToFirst()) {
                return Month(c.getString(0), c.getDouble(1), c.getString(2))
            }
        }
        return null
    }

    fun addExpense(month: String, category: String, amount: Double, note: String?): Long {
        val v = ContentValues().apply {
            put("month", month)
            put("category", category)
            put("amount", amount)
            if (note != null) put("note", note)
            put("ts", System.currentTimeMillis())
        }
        return writableDatabase.insert("expenses", null, v)
    }

    fun expenses(month: String): List<Expense> {
        val out = mutableListOf<Expense>()
        readableDatabase.rawQuery(
            "SELECT id, month, category, amount, note, ts FROM expenses WHERE month = ? ORDER BY ts DESC",
            arrayOf(month)
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Expense(
                    c.getLong(0), c.getString(1), c.getString(2),
                    c.getDouble(3), c.getString(4), c.getLong(5)
                ))
            }
        }
        return out
    }

    fun totalSpent(month: String): Double {
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE month = ?",
            arrayOf(month)
        ).use { c -> if (c.moveToFirst()) return c.getDouble(0) }
        return 0.0
    }

    fun deleteExpense(id: Long) {
        writableDatabase.delete("expenses", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB = "jannah_finance_enc.db"

        // Categories with a religious framing where it fits.
        val CATEGORIES = listOf(
            "housing" to "Housing & utilities",
            "food" to "Food & essentials",
            "transport" to "Transport",
            "family" to "Family support",
            "debt" to "Debt repayment",
            "sadaqah" to "Sadaqah & zakat",
            "savings" to "Savings",
            "personal" to "Personal & misc",
            "waste" to "Wasteful spending"
        )

        fun label(code: String): String =
            CATEGORIES.firstOrNull { it.first == code }?.second ?: code

        // Framing for the wastage category.
        const val WASTE_CAVEAT = "The Qur'an says: \"Indeed, the wasteful are brothers of the devils.\" (Qur'an 17:27). Tracking waste is not about guilt — it is about returning money to where it belongs."
    }
}
