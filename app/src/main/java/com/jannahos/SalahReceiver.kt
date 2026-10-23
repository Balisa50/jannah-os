package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class SalahReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return
        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            putExtra("prayer", prayer)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
