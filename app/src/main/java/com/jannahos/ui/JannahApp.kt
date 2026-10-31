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
import androidx.navigation.NavBackStackEntry
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
    Tab("today", "Focus", Icons.Default.Home),
    Tab("pray",  "Pray",  Icons.Default.DateRange),
    Tab("quran", "Guide", Icons.Default.Menu),
    Tab("you",   "You",   Icons.Default.Person)
)
private val TAB_ROUTES: Set<String> = TABS.map { it.route }.toSet()

private fun NavBackStackEntry.isTab(): Boolean {
    val r = destination.route ?: return false
    return r in TAB_ROUTES
}

@Composable
fun JannahApp() {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "today"

    val showBottomBar = currentRoute in TAB_ROUTES

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
            enterTransition = {
                if (initialState.isTab() && targetState.isTab()) fadeIn(tween(120))
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeIn(tween(200))
            },
            exitTransition = {
                if (initialState.isTab() && targetState.isTab()) fadeOut(tween(120))
                else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeOut(tween(200))
            },
            popEnterTransition = {
                if (initialState.isTab() && targetState.isTab()) fadeIn(tween(120))
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeIn(tween(200))
            },
            popExitTransition = {
                if (initialState.isTab() && targetState.isTab()) fadeOut(tween(120))
                else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeOut(tween(200))
            }
        ) {
            composable("today") { ErrorBoundary("Today") { TodayScreen(onNavigate = goTo) } }
            composable("pray") { ErrorBoundary("Pray") { PrayScreen() } }

            composable("quran") {
                ErrorBoundary("Quran") {
                    QuranTabScreen(
                        onNavigateToSurah = { surah -> nav.navigate("mushaf/${QuranStore.firstPageOfSurah(surah)}/604") },
                        onNavigateToSearch = { nav.navigate("search") },
                        onNavigateToPlan = { nav.navigate("plan") }
                    )
                }
            }

            composable("search") {
                ErrorBoundary("Search") {
                    QuranSearchScreen(onBack = { nav.popBackStack() }, onNavigateToPage = { page -> nav.navigate("mushaf/$page/604") })
                }
            }

            composable("plan") {
                ErrorBoundary("Plan") {
                    KhatmScreen(onBack = { nav.popBackStack() }, onOpenRange = { start, end -> nav.navigate("mushaf/$start/$end") })
                }
            }

            composable("mushaf/{startPage}/{endPage}") { backStackEntry ->
                val startPage = backStackEntry.arguments?.getString("startPage")?.toIntOrNull() ?: 1
                val endPage = backStackEntry.arguments?.getString("endPage")?.toIntOrNull() ?: 604
                ErrorBoundary("Quran Reader") {
                    MushafPageScreen(startPage = startPage, endPage = endPage, onBack = { nav.popBackStack() })
                }
            }

            composable("you") {
                ErrorBoundary("You") {
                    YouScreen(onNavigate = { route -> nav.navigate(route) })
                }
            }
            composable("notes") { ErrorBoundary("Notes") { NotesScreen(onBack = { nav.popBackStack() }) } }
            composable("diary") { ErrorBoundary("Diary") { DiaryScreen(onBack = { nav.popBackStack() }) } }
            composable("backup") { ErrorBoundary("Backup") { BackupScreen(onBack = { nav.popBackStack() }) } }
            composable("weekly") { ErrorBoundary("Weekly") { WeeklyReviewScreen(onBack = { nav.popBackStack() }) } }
            composable("struggle") { ErrorBoundary("Struggle") { StruggleScreen(onBack = { nav.popBackStack() }) } }
            composable("letters") { ErrorBoundary("Letters") { LettersScreen(onBack = { nav.popBackStack() }) } }
            composable("about") { ErrorBoundary("About") { AboutScreen(onBack = { nav.popBackStack() }) } }
            composable("ramadan") { ErrorBoundary("Ramadan") { RamadanScreen(onBack = { nav.popBackStack() }) } }
            composable("sanctuary") { ErrorBoundary("Sanctuary") { SanctuaryScreen(onExit = { nav.popBackStack() }) } }
            composable("adhkar") { ErrorBoundary("Adhkar") { AdhkarScreen(onBack = { nav.popBackStack() }) } }
            composable("sound_profile") { ErrorBoundary("Sound") { SoundProfileScreen(onBack = { nav.popBackStack() }) } }
        }
    }
}
// Trigger rebuild Sat Oct 31 07:28:42 GMT 2026
// Trigger rebuild Sat Oct 31 08:09:31 GMT 2026
// Trigger rebuild Sat Oct 31 08:27:45 GMT 2026
// Trigger rebuild Sat Oct 31 09:10:54 GMT 2026
// Trigger rebuild Sat Oct 31 09:35:05 GMT 2026
// Trigger rebuild Sat Oct 31 09:44:19 GMT 2026
// Trigger rebuild Sat Oct 31 10:25:30 GMT 2026
