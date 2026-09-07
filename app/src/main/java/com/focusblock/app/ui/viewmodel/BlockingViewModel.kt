package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BlockingUiState(
    val blockedPackageName: String = "",
    val blockedAppName: String = "",
    val sessionEndTime: Long = 0L,
    val remainingMillis: Long = 0L,
    val sessionExpired: Boolean = false
)

class BlockingViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(BlockingUiState())
    val uiState: StateFlow<BlockingUiState> = _uiState.asStateFlow()

    fun initialize(packageName: String, appName: String, endTime: Long) {
        _uiState.update { it.copy(blockedPackageName = packageName, blockedAppName = appName, sessionEndTime = endTime, remainingMillis = maxOf(0L, endTime - System.currentTimeMillis())) }
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val endTime = _uiState.value.sessionEndTime
                val remaining = maxOf(0L, endTime - System.currentTimeMillis())
                _uiState.update { it.copy(remainingMillis = remaining) }
                if (remaining == 0L) { _uiState.update { it.copy(sessionExpired = true) }; break }
                delay(1000L)
            }
        }
    }
}
