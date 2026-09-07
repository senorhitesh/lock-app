package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.data.model.InstalledApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppSelectionUiState(
    val allApps: List<InstalledApp> = emptyList(),
    val filteredApps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

class AppSelectionViewModel(app: Application) : AndroidViewModel(app) {
    private val appRepo = (app as FocusBlockApp).appRepository
    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init { loadApps(); observeSelectedPackages() }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = appRepo.getInstalledApps()
            _uiState.update { it.copy(allApps = apps, isLoading = false) }
            applyFilter()
        }
    }

    private fun observeSelectedPackages() {
        viewModelScope.launch { appRepo.blockedPackages.collect { packages -> _uiState.update { it.copy(selectedPackages = packages) } } }
    }

    fun onSearchQueryChanged(query: String) { _uiState.update { it.copy(searchQuery = query) }; applyFilter() }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.lowercase().trim()
        val all = _uiState.value.allApps
        val filtered = if (query.isEmpty()) all
        else all.filter { it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query) }
        _uiState.update { it.copy(filteredApps = filtered) }
    }

    fun onAppToggled(packageName: String) {
        val current = _uiState.value.selectedPackages.toMutableSet()
        if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
        _uiState.update { it.copy(selectedPackages = current) }
    }

    fun selectAll() { _uiState.update { it.copy(selectedPackages = it.filteredApps.map { app -> app.packageName }.toSet()) } }
    fun clearAll() { _uiState.update { it.copy(selectedPackages = emptySet()) } }

    fun saveSelection() {
        viewModelScope.launch { appRepo.saveBlockedPackages(_uiState.value.selectedPackages); _uiState.update { it.copy(isSaved = true) } }
    }
    fun resetSaved() { _uiState.update { it.copy(isSaved = false) } }
}
