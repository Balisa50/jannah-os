package com.jannahos.siraj.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajActionPlannerTest {
    private val now = 1_000_000L
    private val ready = DeviceCapabilities(
        notificationsAllowed = true,
        exactAlarmsAllowed = true,
        accessibilityServiceEnabled = true,
        notificationPolicyAccess = true
    )

    @Test fun planNeverExecutesAndWaitsForConfirmation() {
        val plan = SirajActionPlanner.prepare(
            SirajDeviceAction.SetBedtimeLock(true), "Enable bedtime lock?", ready, now
        )
        assertEquals(SirajActionPlan.State.AWAITING_CONFIRMATION, plan.state)
    }

    @Test fun permissionRevokedWhileDialogOpenBlocksApproval() {
        val plan = SirajActionPlanner.prepare(
            SirajDeviceAction.SetBedtimeLock(true), "Enable bedtime lock?", ready, now
        )
        val approval = SirajActionPlanner.approve(
            plan, true, ready.copy(notificationsAllowed = false), now
        )
        assertTrue(approval is ActionApproval.Blocked)
        assertEquals(
            "Notification permission is required to open the bedtime lock when its alarm fires.",
            (approval as ActionApproval.Blocked).reason
        )
    }

    @Test fun decliningConfirmationCancelsPlan() {
        val plan = SirajActionPlanner.prepare(
            SirajDeviceAction.SetDoNotDisturb(true), "Enable DND?", ready, now
        )
        val approval = SirajActionPlanner.approve(plan, false, ready, now)
        assertTrue(approval is ActionApproval.Cancelled)
        assertEquals(
            SirajActionPlan.State.CANCELLED,
            (approval as ActionApproval.Cancelled).plan.state
        )
    }

    @Test fun cannotApproveTheSamePlanTwice() {
        val plan = SirajActionPlanner.prepare(
            SirajDeviceAction.SetDoNotDisturb(true), "Enable DND?", ready, now
        )
        val approved = SirajActionPlanner.approve(plan, true, ready, now) as ActionApproval.Approved
        val second = SirajActionPlanner.approve(approved.plan, true, ready, now)
        assertTrue(second is ActionApproval.Blocked)
    }

    @Test fun onlyReadyPlansCanBeFinished() {
        val plan = SirajActionPlanner.prepare(
            SirajDeviceAction.SetDoNotDisturb(true), "Enable DND?", ready, now
        )
        val approved = SirajActionPlanner.approve(plan, true, ready, now) as ActionApproval.Approved
        val finished = SirajActionPlanner.finish(approved.plan, ActionExecutionResult.Completed)
        assertEquals(SirajActionPlan.State.COMPLETED, finished.state)
    }
}
