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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jannahos.ui.screens.*
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType

private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val TABS = listOf(
    Tab("today", "Focus", Icons.Default.Home),
    Tab("pray",  "Pray",  Icons.Default.DateRange),
    Tab("quran", "Guide", Icons.Default.Menu),
    Tab("you",   "System", Icons.Default.Person)
)
private val TAB_ROUTES: Set<String> = TABS.map { it.route }.toSet()

private fun NavBackStackEntry.isTab(): Boolean {
    val r = destination.route ?: return false
    return r in TAB_ROUTES
}

@Composable
fun JannahApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "today"
    val showBottomBar = currentRoute in TAB_ROUTES

    val goTo: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
            enterTransition = { fadeIn(tween(120)) },
            exitTransition = { fadeOut(tween(120)) }
        ) {
            composable("today") { TodayScreen(onNavigate = goTo) }
            composable("pray") { PrayScreen() }
            composable("quran") {
                QuranTabScreen(
                    onNavigateToSurah = { surah -> nav.navigate("mushaf/${com.jannahos.QuranStore.firstPageOfSurah(surah)}/604") },
                    onNavigateToSearch = { nav.navigate("search") },
                    onNavigateToPlan = { nav.navigate("plan") }
                )
            }
            composable("search") {
                QuranSearchScreen(onBack = { nav.popBackStack() }, onNavigateToPage = { page -> nav.navigate("mushaf/$page/604") })
            }
            composable("plan") {
                KhatmScreen(onBack = { nav.popBackStack() }, onOpenRange = { start, end -> nav.navigate("mushaf/$start/$end") })
            }
            composable("mushaf/{startPage}/{endPage}") { entry ->
                val startPage = entry.arguments?.getString("startPage")?.toIntOrNull() ?: 1
                val endPage = entry.arguments?.getString("endPage")?.toIntOrNull() ?: 604
                MushafPageScreen(startPage = startPage, endPage = endPage, onBack = { nav.popBackStack() })
            }
            composable("you") { YouScreen(onNavigate = goTo) }
            composable("notes") { ErrorBoundary("Notes") { NotesScreen(onBack = { nav.popBackStack() }) } }
            composable("diary") { ErrorBoundary("Diary") { DiaryScreen(onBack = { nav.popBackStack() }) } }
            composable("backup") { BackupScreen(onBack = { nav.popBackStack() }) }
            composable("weekly") { WeeklyReviewScreen(onBack = { nav.popBackStack() }) }
            composable("struggle") { StruggleScreen(onBack = { nav.popBackStack() }) }
            composable("letters") { LettersScreen(onBack = { nav.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
            composable("ramadan") { RamadanScreen(onBack = { nav.popBackStack() }) }
            composable("sanctuary") { SanctuaryScreen(onExit = { nav.popBackStack() }) }
            composable("adhkar") { AdhkarScreen(onBack = { nav.popBackStack() }) }
            composable("sound_profile") { SoundProfileScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
