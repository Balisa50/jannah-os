package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class SalahReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return
        
        // 1. Arm the lock (persisted, survives process death)
        LockState.arm(context, PrayerLock.durationMinutes(context) * 60_000L)

        // 2. Start Adhan Service (foreground + vibration)
        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            putExtra("prayer", prayer)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 3. Immediate takeover
        val svc = FocusAccessibilityService.instance
        if (svc != null) {
            svc.launchLock()
        } else {
            runCatching { context.startActivity(SalahCommitmentActivity.intent(context).putExtra("prayer", prayer)) }
        }
    }
}
