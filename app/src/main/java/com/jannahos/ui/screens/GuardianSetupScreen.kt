package com.jannahos.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DevicePolicyHelper
import com.jannahos.JannahDeviceAdminReceiver
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun GuardianSetupScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminActive = dpm.isAdminActive(ComponentName(ctx, JannahDeviceAdminReceiver::class.java))

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar("Guardian System", onBack = onBack)

        Column(Modifier.padding(JannahSpacing.gutter)) {
            Text("The Guardian intercepts apps, locks your screen for prayer, and prompts you for Tahajjud evidence without you opening the app.", style = JannahType.body, color = JannahColors.Ink)
            
            Spacer(Modifier.height(JannahSpacing.m))
            
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(12.dp)) {
                Text("To make the locks work, you MUST enable both Device Admin and Accessibility in your phone's settings.", style = JannahType.bodySmall, color = JannahColors.OrangeDeep, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(JannahSpacing.xl))

            // 1. Device Admin
            GuardianItem(
                title = "1. Anti-Uninstall (Device Admin)",
                desc = "Prevents the app from being uninstalled and allows the app to lock your screen.",
                isActive = adminActive,
                onClick = {
                    if (!adminActive) {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(ctx, JannahDeviceAdminReceiver::class.java))
                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Jannah needs this to prevent bypassing locks.")
                        }
                        ctx.startActivity(intent)
                    } else {
                        dpm.removeActiveAdmin(ComponentName(ctx, JannahDeviceAdminReceiver::class.java))
                    }
                }
            )

            Spacer(Modifier.height(JannahSpacing.m))

            // 2. Accessibility
            GuardianItem(
                title = "2. App Blocker (Accessibility)",
                desc = "Intercepts distracting apps and redirects you to Jannah OS.",
                isActive = isAccessibilityServiceEnabled(ctx),
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    ctx.startActivity(intent)
                }
            )

            Spacer(Modifier.height(JannahSpacing.m))


        }
    }
}

@Composable
private fun GuardianItem(title: String, desc: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .pressClickable(onClick = onClick)
            .padding(JannahSpacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JannahColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text(desc, style = JannahType.caption, color = JannahColors.Muted)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isActive) JannahColors.Orange else JannahColors.Border)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(if (isActive) "Active" else "Enable", style = JannahType.micro, color = if (isActive) JannahColors.OnOrange else JannahColors.Muted)
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${com.jannahos.FocusAccessibilityService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(service)
}
