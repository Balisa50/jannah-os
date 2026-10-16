package com.jannahos

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

// User-consented blocking of distracting apps during a prayer window.
// This is NOT spyware. It only triggers while the user is inside a prayer
// commitment they themselves started. It exits the moment the window ends.
class FocusAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        if (!FocusSession.isActive(this)) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in ALLOWED) return

        // Blocked app opened — kick the user back to the commitment screen.
        val back = android.content.Intent(this, SalahCommitmentActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("prayer", FocusSession.currentPrayer(this@FocusAccessibilityService) ?: "fajr")
            putExtra("from_focus", true)
        }
        startActivity(back)
    }

    override fun onInterrupt() {}

    companion object {
        // System UI and essential apps always allowed.
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
