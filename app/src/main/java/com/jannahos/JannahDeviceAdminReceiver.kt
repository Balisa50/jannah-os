package com.jannahos

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class JannahDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Jannah Guardian activated.", Toast.LENGTH_SHORT).show()
    }
    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Guardian deactivated. Protections are off.", Toast.LENGTH_LONG).show()
    }
}
