package com.focusblock.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusblock.app.ui.screen.*

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
        BottomNavItem(Screen.AppSelection, "Apps", Icons.Filled.Apps),
        BottomNavItem(Screen.Schedules, "Schedule", Icons.Filled.Schedule),
        BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
    )
    val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Permissions.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                })
            }
            composable(Screen.Permissions.route) {
                PermissionsScreen(onContinue = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.Permissions.route) { inclusive = true } }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToApps = { navController.navigate(Screen.AppSelection.route) },
                    onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToDuration = { navController.navigate(Screen.Duration.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) }
                )
            }
            composable(Screen.AppSelection.route) { AppSelectionScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Duration.route) {
                DurationScreen(
                    onBack = { navController.popBackStack() },
                    onStarted = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false } } }
                )
            }
            composable(Screen.Schedules.route) { ScheduleScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Settings.route) { SettingsScreen(onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) }) }
            composable(Screen.History.route) { HistoryScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
