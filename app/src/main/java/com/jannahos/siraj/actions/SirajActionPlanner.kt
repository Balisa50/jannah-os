package com.jannahos.siraj.actions

/**
 * A deterministic, inspectable plan for one allowlisted Android operation.
 * Planning never executes an operation. Approval re-checks live capabilities.
 */
data class SirajActionPlan(
    val action: SirajDeviceAction,
    val summary: String,
    val state: State,
    val blockedReason: String? = null
) {
    enum class State { AWAITING_CONFIRMATION, BLOCKED, CANCELLED, READY, COMPLETED, FAILED }
}

sealed interface ActionApproval {
    data class Approved(val plan: SirajActionPlan) : ActionApproval
    data class Blocked(val plan: SirajActionPlan, val reason: String) : ActionApproval
    data class Cancelled(val plan: SirajActionPlan) : ActionApproval
}

object SirajActionPlanner {
    fun prepare(
        action: SirajDeviceAction,
        summary: String,
        capabilities: DeviceCapabilities,
        nowMillis: Long
    ): SirajActionPlan {
        val decision = DeviceActionPolicy.evaluate(
            action = action,
            capabilities = capabilities,
            userConfirmed = false,
            nowMillis = nowMillis
        )
        return when (decision) {
            ActionDecision.NeedsConfirmation -> SirajActionPlan(
                action, summary, SirajActionPlan.State.AWAITING_CONFIRMATION
            )
            ActionDecision.Allowed -> SirajActionPlan(action, summary, SirajActionPlan.State.READY)
            is ActionDecision.Blocked -> SirajActionPlan(
                action, summary, SirajActionPlan.State.BLOCKED, decision.reason
            )
        }
    }

    /**
     * Approval is deliberately separate from preparation. Android capabilities
     * are supplied again so a permission revoked while the dialog is open blocks
     * the operation instead of relying on a stale snapshot.
     */
    fun approve(
        plan: SirajActionPlan,
        confirmed: Boolean,
        freshCapabilities: DeviceCapabilities,
        nowMillis: Long
    ): ActionApproval {
        if (plan.state != SirajActionPlan.State.AWAITING_CONFIRMATION) {
            return ActionApproval.Blocked(plan, "This action plan is no longer awaiting confirmation.")
        }
        if (!confirmed) {
            return ActionApproval.Cancelled(plan.copy(state = SirajActionPlan.State.CANCELLED))
        }
        return when (val decision = DeviceActionPolicy.evaluate(
            action = plan.action,
            capabilities = freshCapabilities,
            userConfirmed = true,
            nowMillis = nowMillis
        )) {
            ActionDecision.Allowed -> ActionApproval.Approved(
                plan.copy(state = SirajActionPlan.State.READY, blockedReason = null)
            )
            ActionDecision.NeedsConfirmation -> ActionApproval.Blocked(
                plan.copy(state = SirajActionPlan.State.BLOCKED),
                "Explicit confirmation is required."
            )
            is ActionDecision.Blocked -> ActionApproval.Blocked(
                plan.copy(state = SirajActionPlan.State.BLOCKED, blockedReason = decision.reason),
                decision.reason
            )
        }
    }

    fun finish(plan: SirajActionPlan, result: ActionExecutionResult): SirajActionPlan {
        require(plan.state == SirajActionPlan.State.READY) {
            "Only an approved action plan can be completed."
        }
        return when (result) {
            ActionExecutionResult.Completed -> plan.copy(state = SirajActionPlan.State.COMPLETED)
            ActionExecutionResult.NeedsConfirmation -> plan.copy(
                state = SirajActionPlan.State.BLOCKED,
                blockedReason = "The executor requested confirmation again."
            )
            is ActionExecutionResult.Blocked -> plan.copy(
                state = SirajActionPlan.State.BLOCKED,
                blockedReason = result.reason
            )
            is ActionExecutionResult.Failed -> plan.copy(
                state = SirajActionPlan.State.FAILED,
                blockedReason = result.reason
            )
        }
    }
}
