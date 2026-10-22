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

        // 1. GLOBAL PRAYER LOCK (Blocks EVERYTHING during Fard prayers)
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

        // 2. CUSTOM APP BLOCKER
        if (AppBlockStore.isBlocked(this, pkg)) {
            val blockIntent = Intent(this, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            startActivity(blockIntent)
            RegretStore.logAttempt(this, pkg)
            return
        }

        // 3. QURAN READING LOCK
        if (QuranReadingLock.shouldLock(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("show_quran_lock", true)
            }
            startActivity(intent)
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
