package com.jannahos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jannahos.ComebackCheck
import com.jannahos.EveningReckoning
import com.jannahos.ReflectionPrompt
import com.jannahos.ui.screens.ComebackScreen
import com.jannahos.ui.screens.EveningScreen
import com.jannahos.ui.screens.MorningReflectionScreen
import com.jannahos.ui.screens.PrayScreen
import com.jannahos.ui.screens.QuranTabScreen
import com.jannahos.ui.screens.TodayScreen
import com.jannahos.ui.screens.YouScreen
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.ScreenScaffold
import kotlinx.coroutines.delay

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("today", "Today", Icons.Default.Home),
    Tab("pray",  "Pray",  Icons.Default.DateRange),
    Tab("quran", "Quran", Icons.Default.Menu),
    Tab("you",   "You",   Icons.Default.Person)
)

@Composable
fun JannahApp() {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "today"

    var currentTabIdx by remember { mutableIntStateOf(0) }
    var isForward by remember { mutableStateOf(true) }

    var comebackState by remember { mutableStateOf<ComebackCheck.State?>(null) }
    var showReflection by remember { mutableStateOf(false) }
    var showEv by remember { mutableStateOf(false) }
    var showComeback by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        try {
            val c = ComebackCheck.check(ctx)
            comebackState = c
            showComeback = c.show
        } catch (_: Throwable) {}
        try { showReflection = ReflectionPrompt.shouldPrompt(ctx) } catch (_: Throwable) {}
        try { showEv = EveningReckoning.shouldShow(ctx) } catch (_: Throwable) {}
    }

    val goTo: (String) -> Unit = { route ->
        val newIdx = TABS.indexOfFirst { it.route == route }
        if (newIdx >= 0) {
            isForward = newIdx >= currentTabIdx
            currentTabIdx = newIdx
            nav.navigate(route) {
                popUpTo(nav.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
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
        bottomBar = { JannahBottomBar(currentRoute, goTo) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "today",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable("today") {
                TabEnter(isForward) {
                    ErrorBoundary("the Today screen") {
                        TodayScreen(onNavigate = goTo)
                    }
                }
            }
            composable("pray") {
                TabEnter(isForward) {
                    ErrorBoundary("the Pray screen") { PrayScreen() }
                }
            }
            composable("quran") {
                TabEnter(isForward) {
                    ErrorBoundary("the Quran tab") { QuranTabScreen() }
                }
            }
            composable("you") {
                TabEnter(isForward) {
                    ErrorBoundary("the You screen") { YouScreen() }
                }
            }
        }
    }
}

@Composable
private fun TabEnter(isForward: Boolean, content: @Composable () -> Unit) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth ->
                if (isForward) fullWidth / 5 else -fullWidth / 5
            },
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(200)),
        exit = ExitTransition.None
    ) {
        content()
    }
}

@Composable
private fun JannahBottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    NavigationBar(
        containerColor = JannahColors.Surface,
        tonalElevation = 0.dp
    ) {
        TABS.forEach { tab ->
            val selected = currentRoute == tab.route
            val tint by animateColorAsState(
                targetValue = if (selected) JannahColors.Orange else JannahColors.Muted,
                animationSpec = tween(180),
                label = "tabTint"
            )
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onTabClick(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = tint
                    )
                },
                label = {
                    Text(
                        tab.label,
                        style = JannahType.micro,
                        color = tint
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
