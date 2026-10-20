package com.jannahos

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in ALWAYS_ALLOWED) return

        // 1. Check if app is explicitly blocked
        if (AppBlockStore.isBlocked(this, pkg)) {
            // Just block it. If they want a schedule, they can toggle it in settings,
            // but for now, if it's in the list, it gets blocked.
            val blockIntent = Intent(this, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            startActivity(blockIntent)
            RegretStore.logAttempt(this, pkg)
            return
        }

        // 2. Check Prayer Lock
        val prayerLock = PrayerLock.enabled(this) && PrayerLock.sessionActive(this)
        if (prayerLock) {
            val prayer = PrayerLock.sessionPrayer(this) ?: "fajr"
            val intent = Intent(this, SalahCommitmentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("prayer", prayer)
            }
            startActivity(intent)
            return
        }

        // 3. Check Quran Reading Lock
        if (QuranReadingLock.shouldLock(this)) {
            // Send user to Jannah OS to do their time
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("show_quran_lock", true)
            }
            startActivity(intent)
            // Go home so they can't look at the blocked app in the background
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
    }

    override fun onInterrupt() {}

    companion object {
        private val ALWAYS_ALLOWED = setOf(
            "com.android.systemui", "com.android.settings", "com.android.phone",
            "com.samsung.android.incallui", "com.android.dialer", "com.samsung.android.dialer",
            "com.android.server.telecom", "android", "com.google.android.dialer",
            "com.android.emergency", "com.jannahos"
        )
    }
}
