package com.focusblock.app.data.repository

import com.focusblock.app.data.db.ScheduleDao
import com.focusblock.app.data.model.Schedule
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    fun getAllSchedules(): Flow<List<Schedule>> = scheduleDao.getAllSchedules()
    suspend fun getEnabledSchedules(): List<Schedule> = scheduleDao.getEnabledSchedules()
    suspend fun getScheduleById(id: Long): Schedule? = scheduleDao.getScheduleById(id)
    suspend fun addSchedule(schedule: Schedule): Long = scheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: Schedule) = scheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: Schedule) = scheduleDao.deleteSchedule(schedule)
    suspend fun deleteScheduleById(id: Long) = scheduleDao.deleteScheduleById(id)
}
