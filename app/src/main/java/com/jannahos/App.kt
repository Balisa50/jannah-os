package com.jannahos

import android.app.Application
import android.os.Environment
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

        // Global crash logger — writes stack traces to /Download/jannah_crashes/
        // so we can diagnose without adb. Never uploaded.
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "jannah_crashes"
                )
                if (!dir.exists()) dir.mkdirs()
                File(dir, "crash_$stamp.txt").writeText(sw.toString())
            } catch (_: Throwable) {}
            prev?.uncaughtException(t, throwable)
        }

        // SQLCipher native lib load off the main thread.
        thread(name = "sqlcipher-init") {
            try { System.loadLibrary("sqlcipher") } catch (_: Throwable) {}
        }
    }
}
