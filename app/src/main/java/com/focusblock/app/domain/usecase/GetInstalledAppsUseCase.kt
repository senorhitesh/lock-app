package com.focusblock.app.domain.usecase

import com.focusblock.app.data.model.InstalledApp
import com.focusblock.app.data.repository.AppRepository

class GetInstalledAppsUseCase(private val appRepository: AppRepository) {
    suspend fun execute(): List<InstalledApp> = appRepository.getInstalledApps()
}
