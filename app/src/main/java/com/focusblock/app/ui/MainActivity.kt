package com.focusblock.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.ui.navigation.AppNavigation
import com.focusblock.app.ui.navigation.Screen
import com.focusblock.app.ui.theme.FocusBlockTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = applicationContext as FocusBlockApp
        lifecycleScope.launch {
            val onboardingComplete = app.appPreferences.onboardingComplete.first()
            val startDestination = if (onboardingComplete) Screen.Home.route else Screen.Onboarding.route
            setContent {
                FocusBlockTheme {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}
