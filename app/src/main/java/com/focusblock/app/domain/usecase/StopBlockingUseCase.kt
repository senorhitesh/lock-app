package com.focusblock.app.domain.usecase

import android.content.Context
import com.focusblock.app.data.model.BlockSession
import com.focusblock.app.data.repository.HistoryRepository
import com.focusblock.app.data.repository.SessionRepository
import com.focusblock.app.receiver.AlarmReceiver
import com.focusblock.app.service.BlockerForegroundService
import com.focusblock.app.utils.AppUtils

sealed class StopBlockingResult {
    object Success : StopBlockingResult()
    object StrictModeActive : StopBlockingResult()
}

class StopBlockingUseCase(
    private val context: Context,
    private val sessionRepository: SessionRepository,
    private val historyRepository: HistoryRepository
) {
    suspend fun execute(session: BlockSession?, forceStop: Boolean = false): StopBlockingResult {
        if (session != null && session.isStrictMode && !forceStop) return StopBlockingResult.StrictModeActive
        if (session != null) {
            val endTime = System.currentTimeMillis()
            for (pkg in session.blockedPackages) {
                historyRepository.addEntry(
                    packageName = pkg,
                    appName = AppUtils.getAppName(context, pkg),
                    startTime = session.startTime,
                    endTime = endTime
                )
            }
        }
        sessionRepository.endSession()
        AlarmReceiver.cancelSessionEnd(context)
        BlockerForegroundService.stopService(context)
        return StopBlockingResult.Success
    }
}
