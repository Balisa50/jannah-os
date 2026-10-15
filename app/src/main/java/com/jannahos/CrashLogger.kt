package com.jannahos

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                    .format(Date())
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                    "jannah_crashes"
                )
                if (!dir.exists()) dir.mkdirs()
                File(dir, "crash_$stamp.txt").writeText(sw.toString())
            } catch (_: Throwable) {
                // Ignore; fall through to default.
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
