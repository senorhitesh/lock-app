package com.focusblock.app.data.repository

import com.focusblock.app.data.db.HistoryDao
import com.focusblock.app.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    fun getAllHistory(): Flow<List<HistoryEntry>> = historyDao.getAllHistory()
    fun getRecentHistory(limit: Int = 100): Flow<List<HistoryEntry>> = historyDao.getRecentHistory(limit)

    suspend fun addEntry(entry: HistoryEntry): Long = historyDao.insertEntry(entry)

    suspend fun addEntry(packageName: String, appName: String, startTime: Long, endTime: Long): Long {
        return historyDao.insertEntry(HistoryEntry(packageName = packageName, appName = appName, startTime = startTime, endTime = endTime))
    }

    fun getHistoryByDay(dayStartMillis: Long): Flow<List<HistoryEntry>> {
        val dayEndMillis = dayStartMillis + 24 * 60 * 60 * 1000L
        return historyDao.getHistoryByDay(dayStartMillis, dayEndMillis)
    }

    suspend fun clearOldHistory(daysToKeep: Int = 30) {
        val cutoff = System.currentTimeMillis() - daysToKeep * 24 * 60 * 60 * 1000L
        historyDao.deleteOlderThan(cutoff)
    }
}
