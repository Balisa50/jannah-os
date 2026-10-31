package com.jannahos

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        @Volatile
        var instance: App? = null
            private set

        fun latestCrash(context: Context): String? {
            val f = java.io.File(context.filesDir, "last_crash.txt")
            return if (f.exists()) f.readText() else null
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // DO NOT schedule alarms here. It crashes the app on startup on some devices.
    }
}
