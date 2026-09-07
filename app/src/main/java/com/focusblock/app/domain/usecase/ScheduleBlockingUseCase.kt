package com.focusblock.app.domain.usecase

import android.content.Context
import com.focusblock.app.data.model.Schedule
import com.focusblock.app.data.repository.ScheduleRepository
import com.focusblock.app.receiver.ScheduleAlarmReceiver

class ScheduleBlockingUseCase(
    private val context: Context,
    private val scheduleRepository: ScheduleRepository
) {
    suspend fun registerAllSchedules() {
        scheduleRepository.getEnabledSchedules().forEach { registerSchedule(it) }
    }

    fun registerSchedule(schedule: Schedule) {
        if (!schedule.isEnabled) {
            ScheduleAlarmReceiver.cancelScheduleAlarms(context, schedule.id)
            return
        }
        ScheduleAlarmReceiver.scheduleAlarms(context, schedule)
    }

    fun unregisterSchedule(scheduleId: Long) {
        ScheduleAlarmReceiver.cancelScheduleAlarms(context, scheduleId)
    }
}
