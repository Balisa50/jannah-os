package com.jannahos

import android.app.NotificationManager
import android.content.Context

object DndHelper {
    private const val PREFS = "jannah_dnd"
    private const val KEY_PRIOR = "prior_filter"
    private const val KEY_PRIOR_VALID = "prior_valid"

    fun enableDnd(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Only record prior state once per lock session — never overwrite on nested enable calls.
        if (!prefs.getBoolean(KEY_PRIOR_VALID, false)) {
            val prior = nm.currentInterruptionFilter
            prefs.edit()
                .putInt(KEY_PRIOR, prior)
                .putBoolean(KEY_PRIOR_VALID, true)
                .apply()
        }
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
    }

    fun disableDnd(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PRIOR_VALID, false)) return
        val prior = prefs.getInt(KEY_PRIOR, NotificationManager.INTERRUPTION_FILTER_ALL)
        nm.setInterruptionFilter(prior)
        prefs.edit().remove(KEY_PRIOR).remove(KEY_PRIOR_VALID).apply()
    }
}
