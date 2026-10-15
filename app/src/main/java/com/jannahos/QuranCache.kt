package com.jannahos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class QuranCache(context: Context) : SQLiteOpenHelper(context, DB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE ayahs (
                surah INTEGER NOT NULL,
                ayah INTEGER NOT NULL,
                arabic TEXT NOT NULL,
                english TEXT NOT NULL,
                PRIMARY KEY (surah, ayah)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS ayahs")
        onCreate(db)
    }

    fun isCached(surah: Int): Boolean {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ayahs WHERE surah = ?",
            arrayOf(surah.toString())
        ).use { c -> if (c.moveToFirst()) return c.getInt(0) > 0 }
        return false
    }

    fun save(surah: Int, ayahs: List<QuranApi.Ayah>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("ayahs", "surah = ?", arrayOf(surah.toString()))
            for (a in ayahs) {
                val v = ContentValues().apply {
                    put("surah", surah)
                    put("ayah", a.number)
                    put("arabic", a.arabic)
                    put("english", a.english)
                }
                db.insert("ayahs", null, v)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun load(surah: Int): List<QuranApi.Ayah> {
        val out = mutableListOf<QuranApi.Ayah>()
        readableDatabase.rawQuery(
            "SELECT ayah, arabic, english FROM ayahs WHERE surah = ? ORDER BY ayah",
            arrayOf(surah.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(QuranApi.Ayah(c.getInt(0), c.getString(1), c.getString(2)))
            }
        }
        return out
    }

    companion object {
        private const val DB = "jannah_quran.db"

        // Name and ayah count for the surahs we ship with. Expandable later.
        val SURAHS = listOf(
            1 to "Al-Fatihah",
            2 to "Al-Baqarah",
            36 to "Ya-Sin",
            55 to "Ar-Rahman",
            67 to "Al-Mulk",
            112 to "Al-Ikhlas",
            113 to "Al-Falaq",
            114 to "An-Nas"
        )
    }
}
