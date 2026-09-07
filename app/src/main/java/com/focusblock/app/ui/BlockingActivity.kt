package com.focusblock.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.focusblock.app.ui.screen.BlockingScreen
import com.focusblock.app.ui.theme.FocusBlockTheme
import com.focusblock.app.ui.viewmodel.BlockingViewModel

class BlockingActivity : ComponentActivity() {

    private val viewModel: BlockingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: packageName
        val endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)

        viewModel.initialize(packageName, appName, endTime)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { goHome() }
        })

        setContent {
            FocusBlockTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                if (uiState.sessionExpired) { finish(); return@FocusBlockTheme }
                BlockingScreen(uiState = uiState, onGoBack = { goHome() })
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_END_TIME = "extra_end_time"

        fun launch(context: Context, blockedPackageName: String, appName: String, endTime: Long) {
            context.startActivity(Intent(context, BlockingActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, blockedPackageName)
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_END_TIME, endTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }
}
