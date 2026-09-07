package com.focusblock.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object AppSelection : Screen("app_selection")
    object Duration : Screen("duration")
    object Schedules : Screen("schedules")
    object Settings : Screen("settings")
    object History : Screen("history")
}
