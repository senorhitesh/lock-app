package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.domain.usecase.CheckPermissionsUseCase
import com.focusblock.app.domain.usecase.PermissionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isStrictMode: Boolean = false,
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false),
    val showStrictModeDialog: Boolean = false
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as FocusBlockApp).appPreferences
    private val checkPermissions = CheckPermissionsUseCase(app)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { prefs.isStrictMode.collect { strict -> _uiState.update { it.copy(isStrictMode = strict) } } }
        refreshPermissions()
    }

    fun refreshPermissions() { _uiState.update { it.copy(permissionStatus = checkPermissions.execute()) } }

    fun onStrictModeToggled() {
        if (!_uiState.value.isStrictMode) _uiState.update { it.copy(showStrictModeDialog = true) }
        else viewModelScope.launch { prefs.setStrictMode(false) }
    }

    fun confirmStrictMode() { viewModelScope.launch { prefs.setStrictMode(true); _uiState.update { it.copy(showStrictModeDialog = false) } } }
    fun dismissStrictModeDialog() { _uiState.update { it.copy(showStrictModeDialog = false) } }
}
