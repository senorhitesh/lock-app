package com.focusblock.app.data.repository

import com.focusblock.app.data.model.BlockSession
import com.focusblock.app.data.prefs.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SessionRepository(private val prefs: AppPreferences) {

    val activeSession: Flow<BlockSession?> = combine(
        prefs.sessionStartTime,
        prefs.sessionEndTime,
        prefs.blockedPackages,
        prefs.sessionStrictMode
    ) { startTime, endTime, blockedPackages, strictMode ->
        if (endTime > 0L && endTime > System.currentTimeMillis()) {
            BlockSession(startTime = startTime, endTime = endTime, blockedPackages = blockedPackages, isStrictMode = strictMode)
        } else null
    }

    suspend fun startSession(endTime: Long, blockedPackages: Set<String>, strictMode: Boolean) {
        prefs.setBlockedPackages(blockedPackages)
        prefs.setSession(startTime = System.currentTimeMillis(), endTime = endTime, strictMode = strictMode)
    }

    suspend fun endSession() { prefs.clearSession() }
    suspend fun getSessionEndTimeNow(): Long = prefs.getSessionEndTimeNow()
    suspend fun getSessionStrictModeNow(): Boolean = prefs.getSessionStrictModeNow()
}
