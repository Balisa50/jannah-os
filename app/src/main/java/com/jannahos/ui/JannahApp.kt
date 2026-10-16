package com.jannahos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    val nav = rememberNavController()
    ScreenScaffold {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = nav,
                startDestination = "today",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("today") {
                    TodayScreen(onNavigate = { route ->
                        nav.navigate(route) { launchSingleTop = true }
                    })
                }
                composable("pray")  { PrayScreen() }
                composable("quran") { QuranTabScreen() }
                composable("dhikr") { DhikrScreen() }
                composable("you")   { YouScreen() }
            }

            JannahBottomBar(
                nav = nav,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun JannahBottomBar(nav: NavHostController, modifier: Modifier = Modifier) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = JannahColors.Surface,
        tonalElevation = 0.dp
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (selected) JannahColors.OrangeSoft else JannahColors.Surface,
                                shape = CircleShape
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) JannahColors.Orange else JannahColors.Muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                label = {
                    Text(
                        tab.label,
                        style = JannahType.micro,
                        color = if (selected) JannahColors.Orange else JannahColors.Muted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = JannahColors.Surface,
                    selectedIconColor = JannahColors.Orange,
                    selectedTextColor = JannahColors.Orange,
                    unselectedIconColor = JannahColors.Muted,
                    unselectedTextColor = JannahColors.Muted
                )
            )
        }
    }
}
