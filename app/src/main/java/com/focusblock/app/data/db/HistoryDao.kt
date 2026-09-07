package com.focusblock.app.data.db

import androidx.room.*
import com.focusblock.app.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY startTime DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime DESC")
    fun getHistoryByDay(dayStart: Long, dayEnd: Long): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history ORDER BY startTime DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HistoryEntry): Long

    @Query("DELETE FROM history WHERE startTime < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}
