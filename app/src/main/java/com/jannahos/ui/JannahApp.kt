package com.jannahos.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jannahos.ui.screens.DhikrScreen
import com.jannahos.ui.screens.PrayScreen
import com.jannahos.ui.screens.QuranTabScreen
import com.jannahos.ui.screens.TodayScreen
import com.jannahos.ui.screens.YouScreen
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType

private data class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val TABS = listOf(
    Tab("today", "Today", Icons.Default.Home),
    Tab("pray", "Pray", Icons.Default.LocationOn),
    Tab("quran", "Quran", Icons.Default.Menu),
    Tab("dhikr", "Dhikr", Icons.Default.Favorite),
    Tab("you", "You", Icons.Default.Person)
)

@Composable
fun JannahApp() {
    val nav = rememberNavController()
    Scaffold(
        containerColor = JannahColors.DayBg,
        bottomBar = { JannahBottomBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "today",
            modifier = Modifier.padding(padding)
        ) {
            composable("today") { TodayScreen() }
            composable("pray") { PrayScreen() }
            composable("quran") { QuranTabScreen() }
            composable("dhikr") { DhikrScreen() }
            composable("you") { YouScreen() }
        }
    }
}

@Composable
private fun JannahBottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar(containerColor = JannahColors.DaySurface) {
        TABS.forEach { tab ->
            NavigationBarItem(
                selected = current == tab.route,
                onClick = {
                    if (current != tab.route) {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = JannahType.tertiary) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JannahColors.Accent,
                    selectedTextColor = JannahColors.Accent,
                    unselectedIconColor = JannahColors.DayMuted,
                    unselectedTextColor = JannahColors.DayMuted,
                    indicatorColor = JannahColors.DayBg
                )
            )
        }
    }
}
