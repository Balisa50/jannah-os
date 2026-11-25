package com.jannahos.siraj.actions

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceActionPolicyTest {
    private val now = 1_000_000L
    private val ready = DeviceCapabilities(
        notificationsAllowed = true,
        exactAlarmsAllowed = true,
        accessibilityServiceEnabled = true,
        notificationPolicyAccess = true
    )

    @Test
    fun stateChangesRequireExplicitConfirmation() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetBedtimeLock(true),
            ready,
            userConfirmed = false,
            nowMillis = now
        )
        assertEquals(ActionDecision.NeedsConfirmation, result)
    }

    @Test
    fun confirmedActionIsAllowedOnlyWhenCapabilitiesExist() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetBedtimeLock(true),
            ready,
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(ActionDecision.Allowed, result)
    }

    @Test
    fun reminderRequiresNotificationAndExactAlarmAccess() {
        val action = SirajDeviceAction.ScheduleReminder("Fajr", now + 60_000)
        val result = DeviceActionPolicy.evaluate(
            action,
            DeviceCapabilities(notificationsAllowed = true),
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(ActionDecision.Blocked("Exact-alarm access is required for this reminder."), result)
    }

    @Test
    fun reminderCannotBeScheduledInThePast() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.ScheduleReminder("Fajr", now),
            ready,
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(ActionDecision.Blocked("The reminder time must be in the future."), result)
    }

    @Test
    fun prayerLockCannotBeEnabledWithoutAccessibilityCapability() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetPrayerLock(true),
            ready.copy(accessibilityServiceEnabled = false),
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(
            ActionDecision.Blocked(
                "Enable the SIRAJ accessibility service in Android settings before enabling Prayer Lock."
            ),
            result
        )
    }

    @Test
    fun disablingPrayerLockDoesNotRequireAccessibilityButStillNeedsConfirmation() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetPrayerLock(false),
            DeviceCapabilities(),
            userConfirmed = false,
            nowMillis = now
        )
        assertEquals(ActionDecision.NeedsConfirmation, result)
    }

    @Test
    fun doNotDisturbRequiresAndroidPolicyAccess() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetDoNotDisturb(true),
            ready.copy(notificationPolicyAccess = false),
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(
            ActionDecision.Blocked("Do Not Disturb access must be granted in Android settings."),
            result
        )
    }

    @Test
    fun bedtimeLockDoesNotRequireNotificationPermission() {
        val result = DeviceActionPolicy.evaluate(
            SirajDeviceAction.SetBedtimeLock(true),
            DeviceCapabilities(exactAlarmsAllowed = true),
            userConfirmed = true,
            nowMillis = now
        )
        assertEquals(ActionDecision.Allowed, result)
    }
}
