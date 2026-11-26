package com.jannahos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.AtomicMoveNotSupportedException

// Partial data export/import. This currently covers preferences, wake events,
// struggle entries, fasts, sleep records and Quran bookmarks only. Do not
// describe this as a complete device backup.
object BackupManager {
    private const val MAX_BACKUP_BYTES = 25L * 1024L * 1024L
    private const val MAX_RECORDS_PER_TABLE = 20_000

    data class Result(val ok: Boolean, val message: String)

    fun export(context: Context, outputFile: File): Result {
        return try {
            val root = JSONObject()
            root.put("schema", 2)
            root.put("exportedAt", System.currentTimeMillis())

            // Preferences
            val prefs = JSONObject()
            prefs.put("lat", Settings.lat(context))
            prefs.put("lon", Settings.lon(context))
            prefs.put("method", Settings.method(context))
            prefs.put("onboarded", Settings.onboardingComplete(context))
            prefs.put("tahajjud_level", TahajjudPlan.level(context))
            prefs.put("tahajjud_nights", TahajjudPlan.nights(context).joinToString(","))
            prefs.put("bedtime_rem", TahajjudPlan.bedtimeReminderOn(context))
            val (bh, bm) = BedtimeStore.get(context)
            prefs.put("bedtime_h", bh)
            prefs.put("bedtime_m", bm)
            prefs.put("adhan", AdhanSettings.current(context))
            val rec = Reciters.current(context)
            prefs.put("reciter", rec.id)
            root.put("prefs", prefs)

            // WakeLog events
            val wakeArr = JSONArray()
            WakeLog(context).lastNights(10000).forEach { e ->
                val o = JSONObject()
                o.put("ts", e.ts)
                o.put("outcome", e.outcome)
                o.put("reason", e.reason ?: JSONObject.NULL)
                o.put("voice", e.voicePath ?: JSONObject.NULL)
                wakeArr.put(o)
            }
            root.put("wake_events", wakeArr)

            // Struggle log
            val strArr = JSONArray()
            StruggleLog(context).recent(10000).forEach { e ->
                val o = JSONObject()
                o.put("ts", e.ts)
                o.put("kind", e.kind)
                o.put("note", e.note ?: JSONObject.NULL)
                strArr.put(o)
            }
            root.put("struggle", strArr)

            // Fasts
            val fastArr = JSONArray()
            val fdb = FastLog(context).readableDatabase
            fdb.rawQuery("SELECT day, kind, created FROM fasts", null).use { c ->
                while (c.moveToNext()) {
                    val o = JSONObject()
                    o.put("day", c.getString(0))
                    o.put("kind", c.getString(1))
                    o.put("created", c.getLong(2))
                    fastArr.put(o)
                }
            }
            root.put("fasts", fastArr)

            // Sleeps
            val sleepArr = JSONArray()
            SleepLog(context).lastNights(10000).forEach { e ->
                val o = JSONObject()
                o.put("day", e.day)
                o.put("bedtime", e.bedtime)
                o.put("waketime", e.waketime)
                o.put("quality", e.quality)
                sleepArr.put(o)
            }
            root.put("sleeps", sleepArr)

            // Bookmarks
            val bmArr = JSONArray()
            QuranBookmarkStore(context).all().forEach { b ->
                val o = JSONObject()
                o.put("surah", b.surah)
                o.put("ayah", b.ayah)
                o.put("note", b.note ?: JSONObject.NULL)
                o.put("created", b.created)
                bmArr.put(o)
            }
            root.put("bookmarks", bmArr)

            val diaryArr = JSONArray()
            DiaryStore(context).allForBackup().forEach { entry ->
                val o = JSONObject()
                o.put("id", entry.id)
                o.put("ts", entry.ts)
                o.put("text", entry.text)
                o.put("tag", entry.tag)
                o.put("reaction", entry.reaction ?: JSONObject.NULL)
                o.put("celebrated", entry.celebrated)
                diaryArr.put(o)
            }
            root.put("diary_entries", diaryArr)

            val backupBytes = root.toString(2).toByteArray(Charsets.UTF_8)
            if (backupBytes.size.toLong() > MAX_BACKUP_BYTES) {
                return Result(false, "Backup exceeds the 25 MB limit and was not written.")
            }
            val destination = outputFile.absoluteFile
            val parent = destination.parentFile ?: error("Backup destination has no parent directory.")
            if (!parent.exists() && !parent.mkdirs()) error("Could not create the backup directory.")
            val temporary = File(parent, destination.name + ".siraj-tmp")
            try {
                FileOutputStream(temporary).use { stream ->
                    stream.write(backupBytes)
                    stream.fd.sync()
                }
                try {
                    Files.move(temporary.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(temporary.toPath(), destination.toPath(), REPLACE_EXISTING)
                }
            } finally {
                if (temporary.exists()) temporary.delete()
            }

            Result(true, "Backup payload prepared: ${wakeArr.length()} wake events, ${strArr.length()} struggle entries, ${fastArr.length()} fasts, ${sleepArr.length()} sleeps, ${bmArr.length()} bookmarks and ${diaryArr.length()} Notes/Diary entries. Other SIRAJ data is not included.")
        } catch (e: Exception) {
            Result(false, "Export failed: ${e.message}")
        }
    }

    /**
     * Validate the complete payload before the importer deletes or replaces
     * any local records. This is not a substitute for encrypted backups, but
     * it prevents malformed/oversized files from causing partial restores.
     */
    private fun validateBackup(root: JSONObject): String? {
        val schema = root.optInt("schema", -1)
        if (schema !in 1..2) return "Unsupported backup version."
        val arrays = listOf("wake_events", "struggle", "fasts", "sleeps", "bookmarks")
        val records = mutableMapOf<String, JSONArray>()
        for (name in arrays) {
            val array = root.optJSONArray(name) ?: return "Backup is incomplete: missing $name."
            if (array.length() > MAX_RECORDS_PER_TABLE) return "Backup contains too many $name records."
            records[name] = array
        }
        if (schema >= 2) {
            val diary = root.optJSONArray("diary_entries") ?: return "Backup is incomplete: missing diary_entries."
            if (diary.length() > MAX_RECORDS_PER_TABLE) return "Backup contains too many Notes/Diary entries."
            records["diary_entries"] = diary
        }
        val prefs = root.optJSONObject("prefs")
        if (prefs != null) {
            val lat = prefs.optDouble("lat", 13.4549)
            val lon = prefs.optDouble("lon", -16.5790)
            if (!lat.isFinite() || lat !in -90.0..90.0 ||
                !lon.isFinite() || lon !in -180.0..180.0
            ) return "Backup contains invalid location preferences."
        }
        return try {
            records.getValue("wake_events").let { a ->
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    o.getLong("ts")
                    require(o.getString("outcome").isNotBlank())
                }
            }
            records.getValue("struggle").let { a ->
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    o.getLong("ts")
                    require(o.getString("kind").isNotBlank())
                }
            }
            records.getValue("fasts").let { a ->
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    require(o.getString("day").isNotBlank())
                    require(o.getString("kind").isNotBlank())
                    o.getLong("created")
                }
            }
            records.getValue("sleeps").let { a ->
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    require(o.getString("day").isNotBlank())
                    o.getLong("bedtime")
                    o.getLong("waketime")
                    require(o.getInt("quality") in 0..5)
                }
            }
            records.getValue("bookmarks").let { a ->
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    require(o.getInt("surah") in 1..114)
                    require(o.getInt("ayah") > 0)
                }
            }
            if (schema >= 2) {
                records.getValue("diary_entries").let { a ->
                    for (i in 0 until a.length()) {
                        val o = a.getJSONObject(i)
                        require(o.getLong("id") > 0)
                        o.getLong("ts")
                        require(o.getString("text").isNotBlank())
                        require(o.getString("tag").isNotBlank())
                        require(o.opt("celebrated") is Boolean)
                    }
                }
            }
            null
        } catch (_: Exception) {
            "Backup data is malformed. No records were changed."
        }
    }

    private fun restoreTable(
        database: net.zetetic.database.sqlcipher.SQLiteDatabase,
        table: String,
        rows: JSONArray,
        valuesFor: (JSONObject) -> android.content.ContentValues,
        conflictAlgorithm: Int = net.zetetic.database.sqlcipher.SQLiteDatabase.CONFLICT_ABORT
    ) {
        database.beginTransaction()
        try {
            database.delete(table, null, null)
            for (index in 0 until rows.length()) {
                val values = valuesFor(rows.getJSONObject(index))
                database.insertWithOnConflict(table, null, values, conflictAlgorithm).also { rowId ->
                    check(rowId != -1L) { "Could not restore $table record." }
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun import(context: Context, inputFile: File): Result {
        return try {
            if (!inputFile.isFile || inputFile.length() !in 1..MAX_BACKUP_BYTES) {
                return Result(false, "Backup file is missing, empty or larger than 25 MB.")
            }
            val text = FileInputStream(inputFile).use { stream ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_BACKUP_BYTES) error("Backup exceeds the 25 MB limit.")
                    output.write(buffer, 0, count)
                }
                output.toByteArray().toString(Charsets.UTF_8)
            }
            val root = JSONObject(text)
            validateBackup(root)?.let { reason -> return Result(false, reason) }

            // Restore each SQLite table atomically. A failure rolls back the
            // current table rather than leaving it half-deleted/half-imported.
            val wakeDb = WakeLog(context).writableDatabase
            restoreTable(
                wakeDb, "events", root.getJSONArray("wake_events"),
                valuesFor = { o ->
                    android.content.ContentValues().apply {
                        put("ts", o.getLong("ts"))
                        put("outcome", o.getString("outcome"))
                        if (!o.isNull("reason")) put("reason", o.getString("reason"))
                        if (!o.isNull("voice")) put("voice_path", o.getString("voice"))
                        put("verified", 0)
                    }
                }
            )

            val stDb = StruggleLog(context).writableDatabase
            restoreTable(
                stDb, "entries", root.getJSONArray("struggle"),
                valuesFor = { o ->
                    android.content.ContentValues().apply {
                        put("ts", o.getLong("ts"))
                        put("kind", o.getString("kind"))
                        if (!o.isNull("note")) put("note", o.getString("note"))
                    }
                }
            )

            val fDb = FastLog(context).writableDatabase
            restoreTable(
                fDb, "fasts", root.getJSONArray("fasts"),
                valuesFor = { o ->
                    android.content.ContentValues().apply {
                        put("day", o.getString("day"))
                        put("kind", o.getString("kind"))
                        put("created", o.getLong("created"))
                    }
                },
                conflictAlgorithm = net.zetetic.database.sqlcipher.SQLiteDatabase.CONFLICT_REPLACE
            )

            val slDb = SleepLog(context).writableDatabase
            restoreTable(
                slDb, "sleeps", root.getJSONArray("sleeps"),
                valuesFor = { o ->
                    android.content.ContentValues().apply {
                        put("day", o.getString("day"))
                        put("bedtime", o.getLong("bedtime"))
                        put("waketime", o.getLong("waketime"))
                        put("quality", o.getInt("quality"))
                        put("created", System.currentTimeMillis())
                    }
                },
                conflictAlgorithm = net.zetetic.database.sqlcipher.SQLiteDatabase.CONFLICT_REPLACE
            )

            val bmDb = QuranBookmarkStore(context).writableDatabase
            restoreTable(
                bmDb, "bookmarks", root.getJSONArray("bookmarks"),
                valuesFor = { o ->
                    android.content.ContentValues().apply {
                        put("surah", o.getInt("surah"))
                        put("ayah", o.getInt("ayah"))
                        if (!o.isNull("note")) put("note", o.getString("note"))
                        put("created", o.optLong("created", System.currentTimeMillis()))
                    }
                }
            )

            // Schema 1 backups predate Notes/Diary coverage, so leave that database untouched.
            if (root.optInt("schema", 1) >= 2) {
                val diaryEntries = root.getJSONArray("diary_entries")
                DiaryStore(context).replaceAll((0 until diaryEntries.length()).map { index ->
                    val o = diaryEntries.getJSONObject(index)
                    DiaryStore.Entry(
                        id = o.getLong("id"),
                        ts = o.getLong("ts"),
                        text = o.getString("text"),
                        tag = o.getString("tag"),
                        reaction = if (o.isNull("reaction")) null else o.getString("reaction"),
                        celebrated = o.optBoolean("celebrated", false)
                    )
                })
            }

            // Apply preferences last so a failed table restore does not leave
            // the user's settings changed while the data restore failed.
            val prefs = root.optJSONObject("prefs")
            if (prefs != null) {
                Settings.set(
                    context,
                    prefs.optDouble("lat", 13.4549),
                    prefs.optDouble("lon", -16.5790),
                    prefs.optString("method", "MWL")
                )
                if (prefs.optBoolean("onboarded", false)) Settings.setOnboardingComplete(context)
                TahajjudPlan.setLevel(context, prefs.optInt("tahajjud_level", 1).coerceIn(1, 5))
                TahajjudPlan.setBedtimeReminder(context, prefs.optBoolean("bedtime_rem", true))
                BedtimeStore.set(
                    context,
                    prefs.optInt("bedtime_h", 22).coerceIn(0, 23),
                    prefs.optInt("bedtime_m", 30).coerceIn(0, 59)
                )
                AdhanSettings.set(context, prefs.optString("adhan", "adhan_short"))
                Reciters.all.firstOrNull { it.id == prefs.optString("reciter", "alafasy") }
                    ?.let { Reciters.set(context, it) }
            }

            Result(true, if (root.optInt("schema", 1) >= 2) {
                "Partial backup restored. Preferences, wake events, struggle entries, fasts, sleep records, Quran bookmarks, Notes and Diary entries were restored. Other SIRAJ data is not included."
            } else {
                "Older partial backup restored. Preferences, wake events, struggle entries, fasts, sleep records and Quran bookmarks were restored. Notes and Diary were left unchanged."
            })
        } catch (e: Exception) {
            Result(
                false,
                "Import failed: ${e.message ?: "unknown error"}. Restore is atomic per table, not across all tables; earlier tables may already have been restored."
            )
        }
    }
}
