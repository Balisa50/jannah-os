package com.jannahos

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        @Volatile
        var instance: App? = null
            private set

        fun latestCrash(context: Context): String? {
            return CrashLogger.latest(context)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Restore persisted prayer-lock timing before any Activity reads it.
        LockState.restore(this)
        CrashLogger.install(this)
    }
}
