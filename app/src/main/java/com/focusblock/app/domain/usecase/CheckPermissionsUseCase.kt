package com.focusblock.app.domain.usecase

import android.content.Context
import com.focusblock.app.utils.PermissionUtils

data class PermissionStatus(
    val hasUsageAccess: Boolean,
    val hasAccessibility: Boolean,
    val hasNotifications: Boolean,
    val hasBatteryOptimizationExemption: Boolean
) {
    val hasMinimumRequired: Boolean get() = hasUsageAccess || hasAccessibility
    val allGranted: Boolean get() = hasUsageAccess && hasAccessibility && hasNotifications && hasBatteryOptimizationExemption
}

class CheckPermissionsUseCase(private val context: Context) {
    fun execute(): PermissionStatus = PermissionStatus(
        hasUsageAccess = PermissionUtils.hasUsageAccess(context),
        hasAccessibility = PermissionUtils.hasAccessibilityService(context),
        hasNotifications = PermissionUtils.hasNotificationPermission(context),
        hasBatteryOptimizationExemption = PermissionUtils.isIgnoringBatteryOptimizations(context)
    )
}
