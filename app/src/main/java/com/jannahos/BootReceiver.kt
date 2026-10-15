package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Date

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val next = TahajjudScheduler.nextScheduled(context)
        if (next > System.currentTimeMillis()) {
            TahajjudScheduler.schedule(context, Date(next))
        }
    }
}
