package com.jannahos

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        PrayerReminderScheduler.scheduleNextReminder(this)
        FridayKahfScheduler.scheduleAll(this)

        // Crash logger. Writes to internal filesDir so no storage permission
        // is needed. A crash report screen reads from here on next launch.
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val dir = File(filesDir, "crashes")
                if (!dir.exists()) dir.mkdirs()
                File(dir, "crash_$stamp.txt").writeText(sw.toString())
                // Keep only the last 5
                dir.listFiles()?.sortedByDescending { it.name }?.drop(5)?.forEach { it.delete() }
            } catch (_: Throwable) {}
            prev?.uncaughtException(t, throwable)
        }

        thread(name = "sqlcipher-init") {
            try { System.loadLibrary("sqlcipher") } catch (_: Throwable) {}
        }
    }

    companion object {
        var instance: App? = null
        fun latestCrash(application: Application): String? {
            return try {
                val dir = File(application.filesDir, "crashes")
                if (!dir.exists()) return null
                val files = dir.listFiles()?.sortedByDescending { it.name } ?: return null
                val newest = files.firstOrNull() ?: return null
                val text = newest.readText()
                newest.delete()
                text
            } catch (_: Throwable) { null }
        }
    }
}
