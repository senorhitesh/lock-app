package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.data.model.HistoryEntry
import com.focusblock.app.utils.TimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val groupedEntries: Map<String, List<HistoryEntry>> = emptyMap(),
    val isLoading: Boolean = true
)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val historyRepo = (app as FocusBlockApp).historyRepository
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepo.getRecentHistory(200).collect { entries ->
                val grouped = entries.groupBy { entry ->
                    val dayStart = (entry.startTime / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
                    TimeUtils.formatDayLabel(dayStart)
                }
                _uiState.update { it.copy(groupedEntries = grouped, isLoading = false) }
            }
        }
    }
}
