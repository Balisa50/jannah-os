package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Settings.isBedtimeLockEnabled(context)) return
        val launchIntent = Intent(context, BedtimeLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(launchIntent)
    }
}
