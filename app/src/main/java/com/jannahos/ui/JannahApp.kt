package com.jannahos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jannahos.ComebackCheck
import com.jannahos.EveningReckoning
import com.jannahos.ReflectionPrompt
import com.jannahos.ui.screens.ComebackScreen
import com.jannahos.ui.screens.DhikrScreen
import com.jannahos.ui.screens.EveningScreen
import com.jannahos.ui.screens.MorningReflectionScreen
import com.jannahos.ui.screens.PrayScreen
import com.jannahos.ui.screens.QuranTabScreen
import com.jannahos.ui.screens.TodayScreen
import com.jannahos.ui.screens.YouScreen
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.ScreenScaffold

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("today", "Today", Icons.Default.Home),
    Tab("pray",  "Pray",  Icons.Default.DateRange),
    Tab("quran", "Quran", Icons.Default.Menu),
    Tab("dhikr", "Dhikr", Icons.Default.Favorite),
    Tab("you",   "You",   Icons.Default.Person)
)

@Composable
fun JannahApp() {
    val ctx = LocalContext.current
    val nav = rememberNavController()

    // Defer every "should this overlay show" check until after first frame.
    // These hit SharedPreferences and the encrypted DB, which is what made
    // the launch feel slow. Now the tab bar paints first, then checks run.
    var comebackState by remember { mutableStateOf<ComebackCheck.State?>(null) }
    var showReflection by remember { mutableStateOf(false) }
    var showEv by remember { mutableStateOf(false) }
    var showComeback by remember { mutableStateOf(false) }
    var checksRan by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        try {
            val c = ComebackCheck.check(ctx)
            comebackState = c
            showComeback = c.show
        } catch (_: Throwable) {}
        try { showReflection = ReflectionPrompt.shouldPrompt(ctx) } catch (_: Throwable) {}
        try { showEv = EveningReckoning.shouldShow(ctx) } catch (_: Throwable) {}
        checksRan = true
    }

    val cs = comebackState
    if (showComeback && cs != null) {
        ScreenScaffold {
            ErrorBoundary("Comeback") {
                ComebackScreen(cs, onDismiss = { showComeback = false })
            }
        }
        return
    }
    if (showReflection) {
        ScreenScaffold {
            ErrorBoundary("Morning reflection") {
                MorningReflectionScreen(onDismiss = { showReflection = false })
            }
        }
        return
    }
    if (showEv) {
        ScreenScaffold {
            ErrorBoundary("Evening reckoning") {
                EveningScreen(onDismiss = { showEv = false })
            }
        }
        return
    }

    Scaffold(
        containerColor = JannahColors.Cream,
        bottomBar = { JannahBottomBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "today",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("today") {
                ErrorBoundary("the Today screen") {
                    TodayScreen(onNavigate = { route ->
                        nav.navigate(route) { launchSingleTop = true }
                    })
                }
            }
            composable("pray") {
                ErrorBoundary("the Pray screen") { PrayScreen() }
            }
            composable("quran") {
                ErrorBoundary("the Quran tab") { QuranTabScreen() }
            }
            composable("dhikr") {
                ErrorBoundary("the Dhikr screen") { DhikrScreen() }
            }
            composable("you") {
                ErrorBoundary("the You screen") { YouScreen() }
            }
        }
    }
}

@Composable
private fun JannahBottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar(
        containerColor = JannahColors.Surface,
        tonalElevation = androidx.compose.ui.unit.Dp(0f)
    ) {
        TABS.forEach { tab ->
            val selected = current == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) JannahColors.Orange else JannahColors.Muted
                    )
                },
                label = {
                    Text(
                        tab.label,
                        style = JannahType.micro,
                        color = if (selected) JannahColors.Orange else JannahColors.Muted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = JannahColors.OrangeSoft,
                    selectedIconColor = JannahColors.Orange,
                    selectedTextColor = JannahColors.Orange,
                    unselectedIconColor = JannahColors.Muted,
                    unselectedTextColor = JannahColors.Muted
                )
            )
        }
    }
}
