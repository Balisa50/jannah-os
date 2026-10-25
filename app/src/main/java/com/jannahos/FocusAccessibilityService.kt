package com.jannahos

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastLaunchAt = 0L

    private val recheck = Runnable {
        if (LockState.isEnforcing() && !LockState.isLockActive) launchLock()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        LockState.restore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!LockState.isEnforcing()) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg in IGNORED) return

        // If the lock screen is already open, re-check in 400ms to ensure the user didn't press Home.
        if (LockState.isLockActive) {
            handler.removeCallbacks(recheck)
            handler.postDelayed(recheck, 400L)
            return
        }
        launchLock()
    }

    fun launchLock() {
        if (!LockState.isEnforcing()) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastLaunchAt < 700L) return // Debounce
        lastLaunchAt = now
        startActivity(Intent(this, SalahCommitmentActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION))
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile var instance: FocusAccessibilityService? = null
            private set

        // Home is deliberately NOT here: pressing Home must snap back.
        private val IGNORED = setOf(
            "android", "com.android.systemui", "com.android.settings", "com.android.phone",
            "com.android.server.telecom", "com.android.dialer", "com.android.incallui",
            "com.google.android.dialer", "com.samsung.android.dialer", "com.samsung.android.incallui"
        )
    }
}
