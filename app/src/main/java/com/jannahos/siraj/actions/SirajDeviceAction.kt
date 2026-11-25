package com.jannahos.siraj.actions

sealed interface SirajDeviceAction {
    data class ScheduleReminder(
        val title: String,
        val triggerAtMillis: Long
    ) : SirajDeviceAction

    data class SetPrayerLock(val enabled: Boolean) : SirajDeviceAction
    data class SetQuranLock(val enabled: Boolean) : SirajDeviceAction
    data class SetBedtimeLock(val enabled: Boolean) : SirajDeviceAction
    data class SetDoNotDisturb(val enabled: Boolean) : SirajDeviceAction
}

data class DeviceCapabilities(
    val notificationsAllowed: Boolean = false,
    val exactAlarmsAllowed: Boolean = false,
    val accessibilityServiceEnabled: Boolean = false,
    val notificationPolicyAccess: Boolean = false
)

sealed interface ActionDecision {
    data object Allowed : ActionDecision
    data object NeedsConfirmation : ActionDecision
    data class Blocked(val reason: String) : ActionDecision
}
