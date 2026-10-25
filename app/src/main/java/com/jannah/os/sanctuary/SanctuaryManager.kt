package com.jannah.os.sanctuary

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object SanctuaryManager {

    enum class Mode(val filter: Int) {
        OFF(NotificationManager.INTERRUPTION_FILTER_ALL),
        PRIORITY(NotificationManager.INTERRUPTION_FILTER_PRIORITY),
        ALARMS(NotificationManager.INTERRUPTION_FILTER_ALARMS)
    }

    fun hasAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun requestAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun apply(context: Context, mode: Mode): Boolean {
        if (!hasAccess(context)) return false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                nm.setInterruptionFilter(mode.filter)
            }
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun release(context: Context) {
        apply(context, Mode.OFF)
    }

    fun currentMode(context: Context): Mode {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return when (nm.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> Mode.PRIORITY
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> Mode.ALARMS
            else -> Mode.OFF
        }
    }
}
