package com.jannahos

import android.app.Application
import kotlin.concurrent.thread

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // SQLCipher native lib load does disk I/O. Off the main thread.
        thread(name = "sqlcipher-init") {
            try { System.loadLibrary("sqlcipher") } catch (_: Throwable) {}
        }
    }
}
