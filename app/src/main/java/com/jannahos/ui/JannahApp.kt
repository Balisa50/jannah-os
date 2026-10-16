package com.jannahos.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.ScreenScaffold

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("today", "Today", Icons.Default.Home),
    Tab("pray",  "Pray",  Icons.Default.LocationOn),
    Tab("quran", "Quran", Icons.Default.Menu),
    Tab("dhikr", "Dhikr", Icons.Default.Favorite),
    Tab("you",   "You",   Icons.Default.Person)
)

@Composable
fun JannahApp() {
    val nav = rememberNavController()
    ScreenScaffold {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                NavHost(
                    navController = nav,
                    startDestination = "today"
                ) {
                    composable("today") { TodayScreen(onNavigate = { route -> nav.navigate(route) { launchSingleTop = true } }) }
                    composable("pray")  { PrayScreen() }
                    composable("quran") { QuranTabScreen() }
                    composable("dhikr") { DhikrScreen() }
                    composable("you")   { YouScreen() }
                }
            }
            JannahBottomBar(nav)
        }
    }
}

@Composable
private fun JannahBottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TABS.forEach { tab ->
            TabItem(
                tab = tab,
                selected = current == tab.route,
                onClick = {
                    if (current != tab.route) {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TabItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) JannahColors.Gold else JannahColors.Muted,
        animationSpec = tween(220),
        label = "icon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) JannahColors.Gold else JannahColors.Faint,
        animationSpec = tween(220),
        label = "label"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.14f else 0f,
        animationSpec = tween(220),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JannahColors.Gold.copy(alpha = glowAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            tab.label,
            style = JannahType.micro,
            color = labelColor
        )
    }
}
