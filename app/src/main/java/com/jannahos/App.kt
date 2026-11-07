package com.jannahos

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        @Volatile
        var instance: App? = null
            private set

        fun latestCrash(context: Context): String? {
            return null
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
