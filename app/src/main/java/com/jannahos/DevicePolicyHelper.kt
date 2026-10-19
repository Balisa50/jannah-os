package com.jannahos

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

object DevicePolicyHelper {
    fun getManager(ctx: Context): DevicePolicyManager =
        ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun isAdminActive(ctx: Context): Boolean =
        getManager(ctx).isAdminActive(ComponentName(ctx, JannahDeviceAdminReceiver::class.java))

    fun lockScreen(ctx: Context) {
        if (isAdminActive(ctx)) {
            getManager(ctx).lockNow()
        }
    }
}
