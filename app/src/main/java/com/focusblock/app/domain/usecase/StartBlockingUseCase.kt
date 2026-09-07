package com.focusblock.app.domain.usecase

import android.content.Context
import com.focusblock.app.data.repository.SessionRepository
import com.focusblock.app.receiver.AlarmReceiver
import com.focusblock.app.service.BlockerForegroundService
import com.focusblock.app.utils.PermissionUtils

sealed class StartBlockingResult {
    object Success : StartBlockingResult()
    object NoAppsSelected : StartBlockingResult()
    object InvalidDuration : StartBlockingResult()
    object PermissionsRequired : StartBlockingResult()
}

class StartBlockingUseCase(
    private val context: Context,
    private val sessionRepository: SessionRepository
) {
    suspend fun execute(blockedPackages: Set<String>, durationMillis: Long, strictMode: Boolean): StartBlockingResult {
        if (blockedPackages.isEmpty()) return StartBlockingResult.NoAppsSelected
        if (durationMillis <= 0) return StartBlockingResult.InvalidDuration
        if (!PermissionUtils.hasMinimumPermissions(context)) return StartBlockingResult.PermissionsRequired

        val endTime = System.currentTimeMillis() + durationMillis
        sessionRepository.startSession(endTime = endTime, blockedPackages = blockedPackages, strictMode = strictMode)
        AlarmReceiver.scheduleSessionEnd(context, endTime)
        BlockerForegroundService.startService(context)
        return StartBlockingResult.Success
    }
}
