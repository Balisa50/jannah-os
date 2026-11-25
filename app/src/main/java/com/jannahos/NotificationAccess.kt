package com.jannahos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object NotificationAccess {
    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
