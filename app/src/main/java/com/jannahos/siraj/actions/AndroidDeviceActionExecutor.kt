package com.jannahos.siraj.actions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.jannahos.BedtimeScheduler
import com.jannahos.DndHelper
import com.jannahos.FocusAccessibilityService
import com.jannahos.PrayerLock
import com.jannahos.QuranReadingLock
import com.jannahos.Settings as AppSettings

sealed interface ActionExecutionResult {
    data object Completed : ActionExecutionResult
    data object NeedsConfirmation : ActionExecutionResult
    data class Blocked(val reason: String) : ActionExecutionResult
    data class Failed(val reason: String) : ActionExecutionResult
}

/**
 * The only entry point for executing SIRAJ device actions.
 *
 * The policy is checked immediately before execution using a fresh snapshot
 * of Android permissions. The caller must pass an explicit confirmation.
 */
object AndroidDeviceActionExecutor {
    fun execute(
        context: Context,
        action: SirajDeviceAction,
        userConfirmed: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): ActionExecutionResult {
        val appContext = context.applicationContext
        val decision = DeviceActionPolicy.evaluate(
            action = action,
            capabilities = readCapabilities(appContext),
            userConfirmed = userConfirmed,
            nowMillis = nowMillis
        )

        when (decision) {
            is ActionDecision.Blocked -> return ActionExecutionResult.Blocked(decision.reason)
            ActionDecision.NeedsConfirmation -> return ActionExecutionResult.NeedsConfirmation
            ActionDecision.Allowed -> Unit
        }

        return runCatching {
            when (action) {
                is SirajDeviceAction.ScheduleReminder -> scheduleReminder(appContext, action)
                is SirajDeviceAction.SetPrayerLock -> PrayerLock.setEnabled(appContext, action.enabled)
                is SirajDeviceAction.SetQuranLock -> QuranReadingLock.setEnabled(appContext, action.enabled)
                is SirajDeviceAction.SetBedtimeLock -> {
                    AppSettings.setBedtimeLockEnabled(appContext, action.enabled)
                    if (action.enabled) BedtimeScheduler.schedule(appContext)
                    else BedtimeScheduler.cancel(appContext)
                }
                is SirajDeviceAction.SetDoNotDisturb -> {
                    if (action.enabled) DndHelper.enableDnd(appContext)
                    else DndHelper.disableDnd(appContext)
                }
            }
            ActionExecutionResult.Completed
        }.getOrElse { error ->
            ActionExecutionResult.Failed(
                error.message?.takeIf { it.isNotBlank() } ?: "Android could not complete this action."
            )
        }
    }

    fun readCapabilities(context: Context): DeviceCapabilities {
        val notificationsAllowed = Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val exactAlarmsAllowed = Build.VERSION.SDK_INT < 31 ||
            (alarmManager?.canScheduleExactAlarms() == true)

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val expectedService = ComponentName(
            context,
            FocusAccessibilityService::class.java
        ).flattenToString()
        val accessibilityEnabled = enabledServices
            .split(':')
            .any { it.equals(expectedService, ignoreCase = true) }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val dndAllowed = notificationManager?.isNotificationPolicyAccessGranted == true

        return DeviceCapabilities(
            notificationsAllowed = notificationsAllowed,
            exactAlarmsAllowed = exactAlarmsAllowed,
            accessibilityServiceEnabled = accessibilityEnabled,
            notificationPolicyAccess = dndAllowed
        )
    }

    private fun scheduleReminder(
        context: Context,
        action: SirajDeviceAction.ScheduleReminder
    ) {
        SirajReminderScheduler.schedule(
            context = context,
            title = action.title,
            triggerAtMillis = action.triggerAtMillis
        )
    }

}
