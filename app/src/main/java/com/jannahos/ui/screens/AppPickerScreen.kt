package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.AppBlockStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean
)

@Composable
fun AppPickerScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    var installed by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selected by remember { mutableStateOf(AppBlockStore.blockedPackages(ctx)) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        installed = withContext(Dispatchers.IO) {
            val pm = ctx.packageManager
            val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            val infos = try { pm.queryIntentActivities(launcherIntent, 0) } catch (e: Exception) { emptyList() }
            infos.mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == ctx.packageName) return@mapNotNull null
                val appInfo = try { pm.getApplicationInfo(pkg, 0) } catch (e: Exception) { null }
                val isSystem = appInfo != null &&
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledApp(
                    packageName = pkg,
                    label = pm.getApplicationLabel(ri.activityInfo.applicationInfo).toString(),
                    isSystem = isSystem
                )
            }.distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
        }
        loaded = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar("Blocked apps", onBack = onBack)

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "${selected.size} app${if (selected.size == 1) "" else "s"} will lock during prayer windows.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted
            )
            Spacer(Modifier.height(JannahSpacing.m))
        }

        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading apps", style = JannahType.caption, color = JannahColors.Muted)
            }
        } else if (installed.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found.", style = JannahType.bodySmall, color = JannahColors.Muted)
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = JannahSpacing.gutter)
            ) {
                items(installed, key = { it.packageName }) { app ->
                    val isSelected = selected.contains(app.packageName)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JannahColors.Card)
                            .clickable {
                                val next = selected.toMutableSet()
                                val added = next.add(app.packageName)
                                if (!added) next.remove(app.packageName)
                                selected = next
                                AppBlockStore.setBlocked(ctx, next)
                            }
                            .padding(horizontal = JannahSpacing.m, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                app.label,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = JannahColors.Ink
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                app.packageName,
                                style = JannahType.micro,
                                color = JannahColors.Faint
                            )
                        }
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) JannahColors.Orange else JannahColors.Border),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("✓",
                                    fontSize = 12.sp,
                                    color = JannahColors.OnOrange,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}
