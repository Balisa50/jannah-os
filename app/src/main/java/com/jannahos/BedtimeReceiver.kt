package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Settings.isBedtimeLockEnabled(context)) return
        BedtimeStore.setLockActive(context, true)
        val launchIntent = Intent(context, BedtimeLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(launchIntent)
    }
}
