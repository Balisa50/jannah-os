package com.jannahos

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val DIRECTORY = "crashes"
    private const val MAX_LOGS = 5

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val trace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val directory = File(appContext.filesDir, DIRECTORY)
                if (!directory.exists()) directory.mkdirs()
                File(directory, "crash_$stamp.txt").writeText(trace.toString())

                directory.listFiles()
                    ?.filter { it.isFile && it.name.startsWith("crash_") }
                    ?.sortedByDescending { it.lastModified() }
                    ?.drop(MAX_LOGS)
                    ?.forEach { it.delete() }
            } catch (_: Exception) {
                // Preserve the platform's normal crash handling if logging fails.
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun latest(context: Context): String? {
        return try {
            File(context.applicationContext.filesDir, DIRECTORY)
                .listFiles()
                ?.filter { it.isFile && it.name.startsWith("crash_") }
                ?.maxByOrNull { it.lastModified() }
                ?.readText()
        } catch (_: Exception) {
            null
        }
    }
}
