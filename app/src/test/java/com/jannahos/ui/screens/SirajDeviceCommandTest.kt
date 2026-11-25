package com.jannahos.ui.screens

import com.jannahos.siraj.actions.SirajDeviceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajDeviceCommandTest {
    @Test
    fun parsesExactPrayerLockCommands() {
        val parsed = parseDeviceAction("enable prayer lock")
        assertEquals(SirajDeviceAction.SetPrayerLock(true), parsed?.first)
    }

    @Test
    fun doesNotTreatAmbiguousPrayerLockTextAsACommand() {
        assertNull(parseDeviceAction("maybe enable prayer lock later"))
    }

    @Test
    fun parsesRelativeReminderAndKeepsItsTitle() {
        val parsed = parseDeviceAction("remind me in 15 minutes to call home")
        val action = parsed?.first as? SirajDeviceAction.ScheduleReminder
        assertEquals("call home", action?.title)
        assertTrue(action!!.triggerAtMillis > System.currentTimeMillis())
    }

    @Test
    fun rejectsZeroMinuteReminder() {
        assertNull(parseDeviceAction("remind me in 0 minutes to call home"))
    }

    @Test
    fun rejectsOverlongReminderInterval() {
        assertNull(parseDeviceAction("remind me in 10081 minutes to call home"))
    }

    @Test
    fun rejectsReminderWithoutTitle() {
        assertNull(parseDeviceAction("remind me in 5 minutes to "))
    }

    @Test
    fun dndDisablePromptExplainsPreviousStateRestoration() {
        val parsed = parseDeviceAction("disable do not disturb")
        assertTrue(parsed?.second?.contains("restore the previous") == true)
    }
}
