package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.data.model.BlockSession
import com.focusblock.app.data.model.InstalledApp
import com.focusblock.app.domain.usecase.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeSession: BlockSession? = null,
    val remainingMillis: Long = 0L,
    val blockedApps: List<InstalledApp> = emptyList(),
    val blockedPackages: Set<String> = emptySet(),
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isStopConfirmVisible: Boolean = false
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val focusApp = app as FocusBlockApp
    private val sessionRepo = focusApp.sessionRepository
    private val appRepo = focusApp.appRepository
    private val checkPermissions = CheckPermissionsUseCase(app)
    private val stopBlocking = StopBlockingUseCase(app, sessionRepo, focusApp.historyRepository)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        observeBlockedPackages()
        refreshPermissions()
        startTimerTicker()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepo.activeSession.collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }
    }

    private fun observeBlockedPackages() {
        viewModelScope.launch {
            appRepo.blockedPackages.collect { packages ->
                _uiState.update { it.copy(blockedPackages = packages) }
                if (packages.isNotEmpty()) loadBlockedApps(packages)
                else _uiState.update { it.copy(blockedApps = emptyList()) }
            }
        }
    }

    private fun loadBlockedApps(packages: Set<String>) {
        viewModelScope.launch {
            val installed = appRepo.getInstalledApps()
            _uiState.update { it.copy(blockedApps = installed.filter { app -> packages.contains(app.packageName) }) }
        }
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (true) {
                val session = _uiState.value.activeSession
                val remaining = session?.remainingMillis ?: 0L
                _uiState.update { it.copy(remainingMillis = remaining) }
                if (session != null && remaining == 0L) {
                    stopBlocking.execute(session, forceStop = true)
                }
                delay(1000L)
            }
        }
    }

    fun refreshPermissions() { _uiState.update { it.copy(permissionStatus = checkPermissions.execute()) } }

    fun onStopClicked() {
        val session = _uiState.value.activeSession ?: return
        if (session.isStrictMode) {
            _uiState.update { it.copy(errorMessage = "Strict Mode is active. You cannot stop the session early.") }
            return
        }
        _uiState.update { it.copy(isStopConfirmVisible = true) }
    }

    fun confirmStop() {
        viewModelScope.launch {
            val session = _uiState.value.activeSession
            when (stopBlocking.execute(session, forceStop = false)) {
                is StopBlockingResult.StrictModeActive -> _uiState.update { it.copy(errorMessage = "Strict Mode is active.", isStopConfirmVisible = false) }
                is StopBlockingResult.Success -> _uiState.update { it.copy(isStopConfirmVisible = false, activeSession = null) }
            }
        }
    }

    fun dismissStopConfirm() { _uiState.update { it.copy(isStopConfirmVisible = false) } }
    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
}
