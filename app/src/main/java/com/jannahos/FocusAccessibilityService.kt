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
        if (pkg in ALLOWED) return

        // Priority 1: Quran reading lock
        if (QuranReadingLock.shouldLock(this)) {
            val back = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("show_quran_lock", true)
            }
            startActivity(back)
            return
        }

        // Priority 2: Prayer lock / Focus mode
        val prayerLock = PrayerLock.enabled(this) && PrayerLock.sessionActive(this)
        val focusWindow = FocusSession.isActive(this)
        if (!prayerLock && !focusWindow) return

        val prayer = FocusSession.currentPrayer(this)
            ?: PrayerLock.sessionPrayer(this)
            ?: "fajr"

        val back = Intent(this, SalahCommitmentActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("prayer", prayer)
            putExtra("from_focus", true)
        }
        startActivity(back)
    }

    override fun onInterrupt() {}

    companion object {
        private val ALLOWED = setOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.samsung.android.incallui",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "android",
            "com.google.android.dialer"
        )
    }
}
