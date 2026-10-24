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
        
        try {
            SalahScheduler.schedule(this, Settings.lat(this), Settings.lon(this))
            PrayerReminderScheduler.scheduleNext(this)
        } catch (_: Exception) {}

        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val dir = File(filesDir, "crashes")
                if (!dir.exists()) dir.mkdirs()
                File(dir, "crash_$stamp.txt").writeText(sw.toString())
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
