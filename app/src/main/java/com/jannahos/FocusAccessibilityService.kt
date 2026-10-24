package com.jannahos

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // 1. If a lock is active, block the notification shade from being pulled down
        if (LockState.isLockActive) {
            performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in ALWAYS_ALLOWED) return

        // 2. Trigger Prayer Lock
        val prayerLock = PrayerLock.enabled(this) && PrayerLock.sessionActive(this)
        if (prayerLock) {
            LockState.isLockActive = true
            val prayer = PrayerLock.sessionPrayer(this) ?: "fajr"
            val intent = Intent(this, SalahCommitmentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("prayer", prayer)
            }
            startActivity(intent)
            return
        }

        // 3. Trigger Quran Lock
        if (QuranReadingLock.shouldLock(this)) {
            LockState.isLockActive = true
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("show_quran_lock", true)
            }
            startActivity(intent)
            return
        }
        
        // 4. Trigger Bedtime Lock
        if (Settings.isBedtimeLockEnabled(this) && BedtimeStore.isLockActive(this)) {
             LockState.isLockActive = true
             val intent = Intent(this, BedtimeLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            return
        }
    }

    override fun onInterrupt() {}

    companion object {
        private val ALWAYS_ALLOWED = setOf(
            "com.android.systemui", "com.android.settings", "com.android.phone",
            "com.samsung.android.incallui", "com.android.dialer", "com.samsung.android.dialer",
            "com.android.server.telecom", "android", "com.google.android.dialer",
            "com.android.emergency", "com.jannahos",
            "com.sec.android.app.launcher", "com.android.launcher3"
        )
    }
}
