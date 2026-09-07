package com.focusblock.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.data.model.Schedule
import com.focusblock.app.domain.usecase.ScheduleBlockingUseCase
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val schedules: List<Schedule> = emptyList(),
    val isLoading: Boolean = true
)

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val scheduleRepo = (app as FocusBlockApp).scheduleRepository
    private val scheduleUseCase = ScheduleBlockingUseCase(app, scheduleRepo)
    private val gson = Gson()
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { scheduleRepo.getAllSchedules().collect { schedules -> _uiState.update { it.copy(schedules = schedules, isLoading = false) } } } }

    fun addSchedule(name: String, daysOfWeek: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, blockedPackages: Set<String>) {
        viewModelScope.launch {
            val schedule = Schedule(name = name, daysOfWeek = daysOfWeek, startHour = startHour, startMinute = startMinute, endHour = endHour, endMinute = endMinute, blockedPackages = gson.toJson(blockedPackages), isEnabled = true)
            val id = scheduleRepo.addSchedule(schedule)
            scheduleUseCase.registerSchedule(schedule.copy(id = id))
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = !schedule.isEnabled)
            scheduleRepo.updateSchedule(updated)
            if (updated.isEnabled) scheduleUseCase.registerSchedule(updated) else scheduleUseCase.unregisterSchedule(updated.id)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch { scheduleUseCase.unregisterSchedule(schedule.id); scheduleRepo.deleteSchedule(schedule) }
    }
}
