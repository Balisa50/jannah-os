package com.jannahos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TahajjudAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val launch = Intent(context, TahajjudAlarmActivity::class.java)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(launch)
    }
}
