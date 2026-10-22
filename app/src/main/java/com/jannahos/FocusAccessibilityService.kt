package com.jannahos

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    
    private var lastLockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        
        // 1. SAFETY: Ignore our own app and allowed system apps
        if (pkg == packageName) return
        if (pkg in ALWAYS_ALLOWED) return

        // 2. SAFETY: 5-second cooldown to stop any accidental loops
        val now = System.currentTimeMillis()
        if (now - lastLockTime < 5000) return

        // 3. PRAYER LOCK
        val prayerLock = PrayerLock.enabled(this) && PrayerLock.sessionActive(this)
        if (prayerLock) {
            val prayer = PrayerLock.sessionPrayer(this) ?: "fajr"
            val intent = Intent(this, SalahCommitmentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("prayer", prayer)
            }
            startActivity(intent)
            lastLockTime = now
            return
        }

        // 4. QURAN LOCK
        if (QuranReadingLock.shouldLock(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("show_quran_lock", true)
            }
            startActivity(intent)
            // Note: I removed performGlobalAction(GLOBAL_ACTION_HOME) here. 
            // It just overlays the lock screen now.
            lastLockTime = now
            return
        }
        
        // 5. BEDTIME LOCK
        if (Settings.isBedtimeLockEnabled(this)) {
             val intent = Intent(this, BedtimeLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            lastLockTime = now
            return
        }
    }

    override fun onInterrupt() {}

    companion object {
        // Added ALL main Samsung, Google, Xiaomi, etc. launchers so the Home screen never triggers a lock.
        private val ALWAYS_ALLOWED = setOf(
            "com.android.systemui", "com.android.settings", "com.android.phone",
            "com.samsung.android.incallui", "com.android.dialer", "com.samsung.android.dialer",
            "com.android.server.telecom", "android", "com.google.android.dialer",
            "com.android.emergency", "com.jannahos",
            "com.sec.android.app.launcher", "com.android.launcher3", 
            "com.google.android.apps.nexuslauncher", "com.android.launcher",
            "com.miui.home", "com.huawei.android.launcher"
        )
    }
}
