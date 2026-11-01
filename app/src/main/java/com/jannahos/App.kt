package com.jannahos

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        @Volatile
        var instance: App? = null
            private set

        fun latestCrash(context: Context): String? {
            // Return null so the app doesn't try to open the crash screen
            return null 
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // No custom crash handlers, no alarm scheduling. Just a clean start.
    }
}
