package com.jannahos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// Backs up all user data to a single JSON file the user chooses.
// On import, all local tables and preferences are restored.
object BackupManager {
    private const val MAX_BACKUP_BYTES = 25L * 1024L * 1024L
    private const val MAX_RECORDS_PER_TABLE = 20_000

    data class Result(val ok: Boolean, val message: String)

    fun export(context: Context, outputFile: File): Result {
        return try {
            val root = JSONObject()
            root.put("schema", 1)
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

            FileOutputStream(outputFile).use { fos ->
                fos.write(root.toString(2).toByteArray())
            }

            Result(true, "Exported ${wakeArr.length()} nights, ${fastArr.length()} fasts, ${sleepArr.length()} sleeps.")
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
        if (root.optInt("schema", -1) != 1) return "Unsupported backup version."
        val arrays = listOf("wake_events", "struggle", "fasts", "sleeps", "bookmarks")
        val records = mutableMapOf<String, JSONArray>()
        for (name in arrays) {
            val array = root.optJSONArray(name) ?: return "Backup is incomplete: missing $name."
            if (array.length() > MAX_RECORDS_PER_TABLE) return "Backup contains too many $name records."
            records[name] = array
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
            null
        } catch (_: Exception) {
            "Backup data is malformed. No records were changed."
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

            // Preferences
            val prefs = root.optJSONObject("prefs")
            if (prefs != null) {
                val lat = prefs.optDouble("lat", 13.4549)
                val lon = prefs.optDouble("lon", -16.5790)
                val method = prefs.optString("method", "MWL")
                Settings.set(context, lat, lon, method)
                if (prefs.optBoolean("onboarded", false)) Settings.setOnboardingComplete(context)
                val lvl = prefs.optInt("tahajjud_level", 1)
                TahajjudPlan.setLevel(context, lvl)
                TahajjudPlan.setBedtimeReminder(context, prefs.optBoolean("bedtime_rem", true))
                BedtimeStore.set(context, prefs.optInt("bedtime_h", 22), prefs.optInt("bedtime_m", 30))
                AdhanSettings.set(context, prefs.optString("adhan", "adhan_short"))
                val recId = prefs.optString("reciter", "alafasy")
                Reciters.all.firstOrNull { it.id == recId }?.let { Reciters.set(context, it) }
            }

            // Wake events, recreate table then insert
            val wakeDb = WakeLog(context).writableDatabase
            wakeDb.delete("events", null, null)
            root.optJSONArray("wake_events")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val values = android.content.ContentValues().apply {
                        put("ts", o.getLong("ts"))
                        put("outcome", o.getString("outcome"))
                        if (!o.isNull("reason")) put("reason", o.getString("reason"))
                        if (!o.isNull("voice")) put("voice_path", o.getString("voice"))
                        put("verified", 0)
                    }
                    wakeDb.insert("events", null, values)
                }
            }

            // Struggle, recreate
            val stDb = StruggleLog(context).writableDatabase
            stDb.delete("entries", null, null)
            root.optJSONArray("struggle")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val values = android.content.ContentValues().apply {
                        put("ts", o.getLong("ts"))
                        put("kind", o.getString("kind"))
                        if (!o.isNull("note")) put("note", o.getString("note"))
                    }
                    stDb.insert("entries", null, values)
                }
            }

            // Fasts
            val fDb = FastLog(context).writableDatabase
            fDb.delete("fasts", null, null)
            root.optJSONArray("fasts")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val values = android.content.ContentValues().apply {
                        put("day", o.getString("day"))
                        put("kind", o.getString("kind"))
                        put("created", o.getLong("created"))
                    }
                    fDb.insertWithOnConflict(
                        "fasts", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
            }

            // Sleeps
            val slDb = SleepLog(context).writableDatabase
            slDb.delete("sleeps", null, null)
            root.optJSONArray("sleeps")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val values = android.content.ContentValues().apply {
                        put("day", o.getString("day"))
                        put("bedtime", o.getLong("bedtime"))
                        put("waketime", o.getLong("waketime"))
                        put("quality", o.getInt("quality"))
                        put("created", System.currentTimeMillis())
                    }
                    slDb.insertWithOnConflict(
                        "sleeps", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
            }

            // Bookmarks
            val bmDb = QuranBookmarkStore(context).writableDatabase
            bmDb.delete("bookmarks", null, null)
            root.optJSONArray("bookmarks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val values = android.content.ContentValues().apply {
                        put("surah", o.getInt("surah"))
                        put("ayah", o.getInt("ayah"))
                        if (!o.isNull("note")) put("note", o.getString("note"))
                        put("created", o.optLong("created", System.currentTimeMillis()))
                    }
                    bmDb.insert("bookmarks", null, values)
                }
            }

            Result(true, "Restored. Reopen the app to see all your data.")
        } catch (e: Exception) {
            Result(false, "Import failed: ${e.message}")
        }
    }
}
