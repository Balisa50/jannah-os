package com.jannahos.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DndHelper
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

@Composable
fun SanctuaryScreen(onExit: () -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val notificationManager = remember(ctx) {
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    var dndAllowed by remember { mutableStateOf(notificationManager.isNotificationPolicyAccessGranted) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dndAllowed = notificationManager.isNotificationPolicyAccessGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Always restore the user's previous notification filter if this screen is
    // removed unexpectedly (for example, navigation or activity teardown).
    DisposableEffect(ctx) {
        onDispose { DndHelper.disableDnd(ctx) }
    }

    var selectedMinutes by remember { mutableStateOf<Int?>(null) }
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    var isActive by remember { mutableStateOf(false) }

    // DND Control
    LaunchedEffect(isActive, dndAllowed) {
        if (isActive) {
            DndHelper.enableDnd(ctx)
        } else {
            DndHelper.disableDnd(ctx)
        }
    }

    // Timer Control
    LaunchedEffect(isActive, selectedMinutes) {
        if (isActive && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            isActive = false // Timer finished
        }
    }

    BackHandler(enabled = isActive) {
        isActive = false
        onExit()
    }

    Box(Modifier.fillMaxSize().background(JannahColors.Cream).padding(24.dp)) {
        if (!isActive) {
            // Selection Screen
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("SIRAJ · FOCUS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(10.dp))
                Text("Sanctuary", fontFamily = FontFamily.Serif, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Make room for what matters. If you grant permission, SIRAJ can enable Do Not Disturb during this session.",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = JannahColors.OrangeDeep,
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp
                )
                if (!dndAllowed) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Do Not Disturb access is off. The focus timer will still work, but notifications will not be silenced.",
                        style = JannahType.caption,
                        color = JannahColors.Muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Allow Do Not Disturb",
                        style = JannahType.button,
                        color = JannahColors.OrangeDeep,
                        modifier = Modifier.pressClickable {
                            ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }.padding(8.dp)
                    )
                }
                
                Spacer(Modifier.height(48.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(15, 25, 45).forEach { mins ->
                        Box(
                            Modifier.size(80.dp).clip(CircleShape)
                                .background(if (selectedMinutes == mins) JannahColors.Orange else JannahColors.Card)
                                .pressClickable { selectedMinutes = mins },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$mins", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (selectedMinutes == mins) JannahColors.OnOrange else JannahColors.Ink)
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))

                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50))
                        .background(if (selectedMinutes != null) JannahColors.Ink else JannahColors.Border)
                        .pressClickable(enabled = selectedMinutes != null) {
                            remainingSeconds = (selectedMinutes ?: 0) * 60L
                            isActive = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Begin", style = JannahType.button, color = JannahColors.OnOrange)
                }

                Spacer(Modifier.height(24.dp))
                
                Text("Cancel", style = JannahType.button, color = JannahColors.Muted, modifier = Modifier.pressClickable { onExit() }.padding(8.dp))
            }
        } else {
            // Active Timer Screen
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("SIRAJ · SANCTUARY ACTIVE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(24.dp))
                
                val mm = remainingSeconds / 60
                val ss = remainingSeconds % 60
                
                Text(
                    String.format(java.util.Locale.US, "%02d:%02d", mm, ss),
                    fontFamily = FontFamily.Serif,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                )
                
                Spacer(Modifier.height(48.dp))
                
                Text(
                    if (dndAllowed) "Your quiet window is active.\nBe present."
                    else "Your focus timer is active.\nNotifications are not silenced.",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp,
                    color = JannahColors.Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(36.dp))
                Box(
                    Modifier.fillMaxWidth().height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(JannahColors.SurfaceAlt)
                        .pressClickable {
                            DndHelper.disableDnd(ctx)
                            isActive = false
                            onExit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("End focus session", style = JannahType.button, color = JannahColors.Ink)
                }
            }
        }
    }
}
