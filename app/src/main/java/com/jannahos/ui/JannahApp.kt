package com.jannahos.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jannahos.*
import com.jannahos.ui.screens.*
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
    val showBottomBar = currentRoute == "today" || currentRoute == "pray" || currentRoute == "you" || currentRoute == "quran"

    var comebackState by remember { mutableStateOf<ComebackCheck.State?>(null) }
    var showReflection by remember { mutableStateOf(false) }
    var showEv by remember { mutableStateOf(false) }
    var showComeback by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        try { val c = ComebackCheck.check(ctx); comebackState = c; showComeback = c.show } catch (_: Throwable) {}
        try { showReflection = ReflectionPrompt.shouldPrompt(ctx) } catch (_: Throwable) {}
        try { showEv = EveningReckoning.shouldShow(ctx) } catch (_: Throwable) {}
    }

    val goTo: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val cs = comebackState
    if (showComeback && cs != null) { ScreenScaffold { ErrorBoundary("Comeback") { ComebackScreen(cs, onDismiss = { showComeback = false }) } }; return }
    if (showReflection) { ScreenScaffold { ErrorBoundary("Morning reflection") { MorningReflectionScreen(onDismiss = { showReflection = false }) } }; return }
    if (showEv) { ScreenScaffold { ErrorBoundary("Evening reckoning") { EveningScreen(onDismiss = { showEv = false }) } }; return }

    Scaffold(
        containerColor = JannahColors.Cream,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = JannahColors.Surface, tonalElevation = 0.dp) {
                    TABS.forEach { tab ->
                        val selected = currentRoute == tab.route
                        val tint by animateColorAsState(
                            targetValue = if (selected) JannahColors.Orange else JannahColors.Muted,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tabTint"
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (!selected) goTo(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label, tint = tint) },
                            label = { Text(tab.label, style = JannahType.micro, color = tint) },
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
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "today",
            modifier = Modifier.fillMaxSize().padding(padding),
            // SILKY SMOOTH TRANSITIONS
            enterTransition = { 
                slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f)) + fadeIn(tween(200)) 
            },
            exitTransition = { 
                slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f)) + fadeOut(tween(200)) 
            },
            popEnterTransition = { 
                slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f)) + fadeIn(tween(200)) 
            },
            popExitTransition = { 
                slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f)) + fadeOut(tween(200)) 
            }
        ) {
            composable("today") { ErrorBoundary("Today") { TodayScreen(onNavigate = goTo) } }
            composable("pray") { ErrorBoundary("Pray") { PrayScreen() } }
            composable("quran") { ErrorBoundary("Quran") { QuranTabScreen(onNavigateToSurah = { surahId -> nav.navigate("mushaf/${QuranStore.firstPageOfSurah(surahId)}") }) } }
            composable("you") { ErrorBoundary("You") { YouScreen() } }
            composable("mushaf/{startPage}") { backStackEntry ->
                val startPage = backStackEntry.arguments?.getString("startPage")?.toIntOrNull() ?: 1
                ErrorBoundary("Quran Reader") { MushafPageScreen(startPage = startPage, onBack = { nav.popBackStack() }) }
            }
        }
    }
}
