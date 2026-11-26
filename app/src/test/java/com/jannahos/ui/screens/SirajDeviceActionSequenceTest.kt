package com.jannahos.ui.screens

import com.jannahos.siraj.actions.SirajDeviceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajDeviceActionSequenceTest {
    @Test fun parsesSupportedStepsInOrder() {
        val sequence = parseDeviceActionSequence(
            "enable prayer lock then enable bedtime lock"
        )
        assertEquals(2, sequence?.size)
        assertEquals(SirajDeviceAction.SetPrayerLock(true), sequence?.get(0)?.first)
        assertEquals(SirajDeviceAction.SetBedtimeLock(true), sequence?.get(1)?.first)
    }

    @Test fun rejectsSequenceIfAnyStepIsUnsupported() {
        assertNull(
            parseDeviceActionSequence("enable prayer lock then launch arbitrary shell command")
        )
    }

    @Test fun doesNotTreatSingleActionAsASequence() {
        assertNull(parseDeviceActionSequence("enable prayer lock"))
    }
}
