package com.jannahos.siraj.actions

/**
 * The policy layer is evaluated before any Android API is called.
 * All state-changing actions require a separate, explicit user confirmation.
 * Missing OS capabilities block execution; an AI model cannot grant them.
 */
object DeviceActionPolicy {
    fun evaluate(
        action: SirajDeviceAction,
        capabilities: DeviceCapabilities,
        userConfirmed: Boolean,
        nowMillis: Long
    ): ActionDecision {
        val capabilityFailure = when (action) {
            is SirajDeviceAction.ScheduleReminder -> when {
                action.title.isBlank() -> "A reminder needs a title."
                action.triggerAtMillis <= nowMillis -> "The reminder time must be in the future."
                !capabilities.notificationsAllowed -> "Notification permission is required."
                !capabilities.exactAlarmsAllowed -> "Exact-alarm access is required for this reminder."
                else -> null
            }

            is SirajDeviceAction.SetPrayerLock ->
                if (action.enabled && !capabilities.accessibilityServiceEnabled) {
                    "Enable the SIRAJ accessibility service in Android settings before enabling Prayer Lock."
                } else null

            is SirajDeviceAction.SetQuranLock ->
                if (action.enabled && !capabilities.exactAlarmsAllowed) {
                    "Exact-alarm access is required for Quran reading reminders."
                } else if (action.enabled && !capabilities.notificationsAllowed) {
                    "Notification permission is required for Quran reading reminders."
                } else null

            is SirajDeviceAction.SetBedtimeLock ->
                if (action.enabled && !capabilities.exactAlarmsAllowed) {
                    "Exact-alarm access is required for the bedtime lock."
                } else null

            is SirajDeviceAction.SetDoNotDisturb ->
                if (!capabilities.notificationPolicyAccess) {
                    "Do Not Disturb access must be granted in Android settings."
                } else null
        }

        if (capabilityFailure != null) return ActionDecision.Blocked(capabilityFailure)
        return if (userConfirmed) ActionDecision.Allowed else ActionDecision.NeedsConfirmation
    }
}
