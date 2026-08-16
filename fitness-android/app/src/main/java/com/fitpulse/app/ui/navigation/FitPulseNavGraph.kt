package com.fitpulse.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitpulse.app.ui.screen.ai.AiScreen
import com.fitpulse.app.ui.screen.auth.LoginScreen
import com.fitpulse.app.ui.screen.health.HealthScreen
import com.fitpulse.app.ui.screen.home.HomeScreen
import com.fitpulse.app.ui.screen.profile.ProfileScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Destinations.HOME, "训练", Icons.Default.FitnessCenter),
    BottomNavItem(Destinations.HEALTH, "健康", Icons.Default.Favorite),
    BottomNavItem(Destinations.AI, "AI", Icons.Default.AutoAwesome),
    BottomNavItem(Destinations.PROFILE, "我的", Icons.Default.Person)
)

@Composable
fun FitPulseNavGraph(
    startDestination: String = Destinations.LOGIN
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    var showBottomBar by remember { mutableStateOf(false) }
    LaunchedEffect(currentDest) {
        showBottomBar = bottomNavItems.any { it.route == currentDest?.route }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Destinations.HOME) { HomeScreen(vm = hiltViewModel()) }
            composable(Destinations.HEALTH) { HealthScreen(vm = hiltViewModel()) }
            composable(Destinations.AI) { AiScreen() }
            composable(Destinations.PROFILE) { ProfileScreen(vm = hiltViewModel(), onLogout = { navController.navigate(Destinations.LOGIN) { popUpTo(0) } }) }
        }
    }
}
